package org.example.distributedcomputing.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.distributedcomputing.integration.config.SpringBootTestContainers;
import org.example.distributedcomputing.model.dto.CreatorDTO;
import org.example.distributedcomputing.model.dto.exception.ErrorResponseDTO;
import org.example.distributedcomputing.model.entity.Creator;
import org.example.distributedcomputing.repository.CreatorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
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
class CreatorControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CreatorRepository creatorRepository;
    private Creator creator;

    @BeforeEach
    void beforeEach() {
        creator = createCreator();
    }

    @AfterEach
    void afterEach() {
        creatorRepository.deleteAll();
    }

    @Test
    void saveOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setId(-1L);
        creatorDTO.setFirstname("f");
        creatorDTO.setLastname("l");
        creatorDTO.setLogin("l");
        creatorDTO.setPassword("p");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals("Validation Error", errorResponseDTO.getMessage());
        assertEquals(5, invalidFields.size());
        assertTrue(invalidFields.containsKey("id"));
        assertTrue(invalidFields.containsKey("login"));
        assertTrue(invalidFields.containsKey("password"));
        assertTrue(invalidFields.containsKey("firstname"));
        assertTrue(invalidFields.containsKey("lastname"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Login must be between 2 and 64 characters", invalidFields.get("login"));
        assertEquals("Firstname must be between 2 and 64 characters", invalidFields.get("firstname"));
        assertEquals("Lastname must be between 2 and 64 characters", invalidFields.get("lastname"));
        assertEquals("Password must be between 8 and 128 characters", invalidFields.get("password"));
    }

    @Test
    void saveOne_returnsForbidden_whenLoginExists() throws Exception {
        // given
        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setFirstname("firstName");
        creatorDTO.setLastname("second");
        creatorDTO.setLogin(creator.getLogin());
        creatorDTO.setPassword("password");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals("Login already exists", errorResponseDTO.getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponseDTO.getCode());
    }

    @Test
    void saveOne_returnsCreated() throws Exception {
        // given
        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setFirstname("firstName");
        creatorDTO.setLastname("second");
        creatorDTO.setLogin("login");
        creatorDTO.setPassword("password");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();
        // then
        CreatorDTO responseCreatorDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), CreatorDTO.class);

        assertNotNull(responseCreatorDTO.getId());
        assertEquals(creatorDTO.getFirstname(), responseCreatorDTO.getFirstname());
        assertEquals(creatorDTO.getLastname(), responseCreatorDTO.getLastname());
        assertEquals(creatorDTO.getLogin(), responseCreatorDTO.getLogin());
        assertEquals(creatorDTO.getPassword(), responseCreatorDTO.getPassword());
    }

    @Test
    void getOne_returnsNotFound_whenCreatorNotFound() throws Exception {
        // given
        Long notExistId = 100L;
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/creators/{id}", notExistId))
                .andExpect(status().isNotFound())
                .andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals("Creator not found", errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void getOne_returnsCreator() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/creators/{id}", creator.getId()))
                .andExpect(status().isOk())
                .andReturn();
        // then
        CreatorDTO responseCreatorDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), CreatorDTO.class);

        assertEquals(creator.getId(), responseCreatorDTO.getId());
        assertEquals(creator.getFirstname(), responseCreatorDTO.getFirstname());
        assertEquals(creator.getLastname(), responseCreatorDTO.getLastname());
        assertEquals(creator.getLogin(), responseCreatorDTO.getLogin());
        assertEquals(creator.getPassword(), responseCreatorDTO.getPassword());
    }

    @Test
    void getAll_returnsCreators() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/creators"))
                .andExpect(status().isOk())
                .andReturn();
        // then
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<CreatorDTO> creatorDTOs = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, CreatorDTO.class));

        assertEquals(1, creatorDTOs.size());
    }

    @Test
    void deleteOne_returnsNotFound_whenCreatorNotFound() throws Exception {
        // given
        Long notExistId = 100L;
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/creators/{id}", notExistId))
                .andExpect(status().isNotFound())
                .andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals("Creator not found", errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void deleteOne_deletesCreator() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/creators/{id}", creator.getId()))
                .andExpect(status().isNoContent());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/creators/{id}", creator.getId()))
                .andExpect(status().isNotFound()).andReturn();
        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertFalse(creatorRepository.existsById(creator.getId()));
        assertEquals("Creator not found", errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setId(-1L);
        creatorDTO.setFirstname("f");
        creatorDTO.setLastname("l");
        creatorDTO.setLogin("l");
        creatorDTO.setPassword("p");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest()).andReturn();

        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals("Validation Error", errorResponseDTO.getMessage());
        assertEquals(5, invalidFields.size());
        assertTrue(invalidFields.containsKey("id"));
        assertTrue(invalidFields.containsKey("login"));
        assertTrue(invalidFields.containsKey("password"));
        assertTrue(invalidFields.containsKey("firstname"));
        assertTrue(invalidFields.containsKey("lastname"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Login must be between 2 and 64 characters", invalidFields.get("login"));
        assertEquals("Firstname must be between 2 and 64 characters", invalidFields.get("firstname"));
        assertEquals("Lastname must be between 2 and 64 characters", invalidFields.get("lastname"));
        assertEquals("Password must be between 8 and 128 characters", invalidFields.get("password"));
    }

    @Test
    void updateOne_returnsNotFound_whenCreatorNotFound() throws Exception {
        // given
        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setId(100L);
        creatorDTO.setFirstname("updated-firstname");
        creatorDTO.setLastname("updated-lastname");
        creatorDTO.setLogin("updated-login");
        creatorDTO.setPassword("updated-password");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals("Creator not found", errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsForbidden_whenLoginExists() throws Exception {
        // given
        Creator saved = creatorRepository.save(Creator.builder()
                .login("login")
                .password("password")
                .firstname("firstname")
                .lastname("lastname")
                .build());

        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setId(saved.getId());
        creatorDTO.setFirstname("updated-firstname");
        creatorDTO.setLastname("updated-lastname");
        creatorDTO.setLogin(creator.getLogin());
        creatorDTO.setPassword("updated-password");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals("Login already exists", errorResponseDTO.getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsUpdatedCreator() throws Exception {
        // given
        CreatorDTO creatorDTO = new CreatorDTO();
        creatorDTO.setId(creator.getId());
        creatorDTO.setFirstname("updated-firstname");
        creatorDTO.setLastname("updated-lastname");
        creatorDTO.setLogin("updated-login");
        creatorDTO.setPassword("updated-password");

        String requestBody = objectMapper.writeValueAsString(creatorDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/creators")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        // then
        CreatorDTO responseCreatorDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), CreatorDTO.class);

        assertEquals(creatorDTO.getId(), responseCreatorDTO.getId());
        assertEquals(creatorDTO.getFirstname(), responseCreatorDTO.getFirstname());
        assertEquals(creatorDTO.getLastname(), responseCreatorDTO.getLastname());
        assertEquals(creatorDTO.getLogin(), responseCreatorDTO.getLogin());
        assertEquals(creatorDTO.getPassword(), responseCreatorDTO.getPassword());
    }


    private Creator createCreator() {
        Creator creator = Creator.builder()
                .firstname("init-firstname")
                .lastname("init-lastname")
                .login("init-login")
                .password("init-password")
                .build();
        return creatorRepository.save(creator);
    }
}
