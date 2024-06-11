package dev.makos.discussion.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.makos.discussion.integration.config.SpringBootTestContainers;
import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.dto.exception.ErrorResponseDTO;
import dev.makos.discussion.model.entity.Comment;
import dev.makos.discussion.model.entity.CommentKey;
import dev.makos.discussion.repository.CommentRepository;
import dev.makos.discussion.repository.IdRepository;
import dev.makos.discussion.util.ErrorMessage;
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
class CommentControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private IdRepository idRepository;
    private Comment comment;

    @BeforeEach
    void setUp() {
        idRepository.increment("comment_id");
        Long commentId = idRepository.getCurrentId("comment_id");

        CommentKey commentKey = new CommentKey("KZ", 1L, commentId);
        Comment comment = new Comment(commentKey, "init-comment");
        this.comment = commentRepository.save(comment);
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        idRepository.deleteAll();
    }

    @Test
    void getOne_returnsNotFound_whenCommentNotFound() throws Exception {
        // given
        Long notExistId = 100L;

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/comments/{id}", notExistId))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void getOne_returnsComment() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/comments/{id}", comment.getId()))
                .andExpect(status().isOk()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        CommentDTO foundDTO = objectMapper.readValue(contentAsString, CommentDTO.class);
        assertNotNull(foundDTO);
        assertEquals(comment.getId(), foundDTO.getId());
        assertEquals(comment.getContent(), foundDTO.getContent());
        assertEquals(comment.getTweetId(), foundDTO.getTweetId());
        assertEquals(comment.getCountry(), foundDTO.getCountry());
    }

    @Test
    void getAll_returnsComments() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/comments"))
                .andExpect(status().isOk()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<CommentDTO> foundDTOs = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, CommentDTO.class));

        assertNotNull(foundDTOs);
        assertEquals(1, foundDTOs.size());
        CommentDTO foundDTO = foundDTOs.getFirst();
        assertEquals(comment.getId(), foundDTO.getId());
        assertEquals(comment.getContent(), foundDTO.getContent());
        assertEquals(comment.getTweetId(), foundDTO.getTweetId());
        assertEquals(comment.getCountry(), foundDTO.getCountry());
    }

    @Test
    void deleteOne_deletesComment() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/comments/{id}", comment.getId()))
                .andExpect(status().isNoContent());
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/comments/{id}", comment.getId()))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertFalse(commentRepository.findCommentById(comment.getId()).isPresent());
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void deleteOne_returnsNotFound_whenCommentNotFound() throws Exception {
        // given
        Long notExistId = 100L;

        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/comments/{id}", notExistId))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void saveOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(3, invalidFields.size());
        assertTrue(invalidFields.containsKey("country"));
        assertTrue(invalidFields.containsKey("tweetId"));
        assertTrue(invalidFields.containsKey("content"));
        assertEquals("Country is not provided", invalidFields.get("country"));
        assertEquals("Tweet ID is not provided", invalidFields.get("tweetId"));
        assertEquals("Content is not provided", invalidFields.get("content"));
    }

    @Test
    void saveOne_returnsBadRequest_whenMinValueValidationFailed() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCountry("KZ");
        commentDTO.setTweetId(-1L);
        commentDTO.setContent("content");

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(1, invalidFields.size());
        assertTrue(invalidFields.containsKey("tweetId"));
        assertEquals("Tweet ID must be greater than 0", invalidFields.get("tweetId"));
    }

    @Test
    void saveOne_returnsComment() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCountry("KZ");
        commentDTO.setTweetId(1L);
        commentDTO.setContent("content");

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        CommentDTO savedDTO = objectMapper.readValue(contentAsString, CommentDTO.class);

        assertNotNull(savedDTO);
        assertNotNull(savedDTO.getId());
        assertEquals(commentDTO.getCountry(), savedDTO.getCountry());
        assertEquals(commentDTO.getTweetId(), savedDTO.getTweetId());
        assertEquals(commentDTO.getContent(), savedDTO.getContent());
    }

    @Test
    void updateOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(-1L);

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(4, invalidFields.size());
        assertTrue(invalidFields.containsKey("id"));
        assertTrue(invalidFields.containsKey("country"));
        assertTrue(invalidFields.containsKey("tweetId"));
        assertTrue(invalidFields.containsKey("content"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Country is not provided", invalidFields.get("country"));
        assertEquals("Tweet ID is not provided", invalidFields.get("tweetId"));
        assertEquals("Content is not provided", invalidFields.get("content"));
    }

    @Test
    void updateOne_returnsNotFound_whenCommentNotFound() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(100L);
        commentDTO.setCountry("KZ");
        commentDTO.setTweetId(1L);
        commentDTO.setContent("content");

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsComment() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setCountry(comment.getCountry());
        commentDTO.setTweetId(comment.getTweetId());
        commentDTO.setContent("updated-content");

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        CommentDTO updatedDTO = objectMapper.readValue(contentAsString, CommentDTO.class);

        assertNotNull(updatedDTO);
        assertEquals(commentDTO.getContent(), updatedDTO.getContent());
    }
}