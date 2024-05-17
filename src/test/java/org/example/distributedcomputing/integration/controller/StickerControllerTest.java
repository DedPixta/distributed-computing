package org.example.distributedcomputing.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.distributedcomputing.integration.config.SpringBootTestContainers;
import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.model.dto.exception.ErrorResponseDTO;
import org.example.distributedcomputing.model.entity.Sticker;
import org.example.distributedcomputing.repository.StickerRepository;
import org.example.distributedcomputing.util.ErrorMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTestContainers
class StickerControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StickerRepository stickerRepository;

    Sticker sticker;

    @BeforeEach
    void beforeEach() {
        sticker = createSticker();
    }

    @AfterEach
    void afterEach() {
        stickerRepository.deleteAll();
    }

    @Test
    void saveOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        StickerDTO stickerDTO = new StickerDTO();
        stickerDTO.setId(-1L);
        stickerDTO.setName("n");

        String requestBody = objectMapper.writeValueAsString(stickerDTO);

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/stickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("id"));
        assertTrue(invalidFields.containsKey("name"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Name must be between 2 and 32 characters", invalidFields.get("name"));
    }

    @Test
    void saveOne_returnsCreated() throws Exception {
        // given
        StickerDTO stickerDTO = new StickerDTO();
        stickerDTO.setName("Sticker");

        String requestBody = objectMapper.writeValueAsString(stickerDTO);

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/stickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        StickerDTO response = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), StickerDTO.class);

        assertNotNull(response.getId());
        assertEquals(stickerDTO.getName(), response.getName());
    }

    @Test
    void getOne_returnsNotFound_whenStickerNotFound() throws Exception {
        // given
        Long notExistId = 100L;

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/stickers/{id}", notExistId))
                .andExpect(status().isNotFound()).andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void getOne_returnsSticker() throws Exception {
        // given
        Long id = sticker.getId();

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/stickers/{id}", id))
                .andExpect(status().isOk()).andReturn();

        StickerDTO response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), StickerDTO.class);

        assertEquals(sticker.getId(), response.getId());
        assertEquals(sticker.getName(), response.getName());
    }

    @Test
    void getAll_returnsStickers() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/stickers"))
                .andExpect(status().isOk()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<StickerDTO> stickerDTOS = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, StickerDTO.class));

        assertEquals(1, stickerDTOS.size());
    }

    @Test
    void deleteOne_returnsNotFound_whenStickerNotFound() throws Exception {
        // given
        Long notExistId = 100L;

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/stickers/{id}", notExistId))
                .andExpect(status().isNotFound()).andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void deleteOne_deleteSticker() throws Exception {
        // given
        Long id = sticker.getId();

        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/stickers/{id}", id))
                .andExpect(status().isNoContent());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/stickers/{id}", id))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertFalse(stickerRepository.existsById(id));
        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        StickerDTO stickerDTO = new StickerDTO();
        stickerDTO.setId(-1L);
        stickerDTO.setName("n");

        String requestBody = objectMapper.writeValueAsString(stickerDTO);

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/stickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("id"));
        assertTrue(invalidFields.containsKey("name"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Name must be between 2 and 32 characters", invalidFields.get("name"));
    }

    @Test
    void updateOne_returnsNotFound_whenStickerNotFound() throws Exception {
        // given
        StickerDTO stickerDTO = new StickerDTO();
        stickerDTO.setId(100L);
        stickerDTO.setName("Sticker");

        String requestBody = objectMapper.writeValueAsString(stickerDTO);

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/stickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_updateSticker() throws Exception {
        // given
        StickerDTO stickerDTO = new StickerDTO();
        stickerDTO.setId(sticker.getId());
        stickerDTO.setName("Updated Sticker");

        String requestBody = objectMapper.writeValueAsString(stickerDTO);

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/stickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        StickerDTO response = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), StickerDTO.class);

        assertEquals(stickerDTO.getId(), response.getId());
        assertEquals(stickerDTO.getName(), response.getName());
    }

    private Sticker createSticker() {
        Sticker sticker = new Sticker();
        sticker.setName("init-name");
        return stickerRepository.save(sticker);
    }
}
