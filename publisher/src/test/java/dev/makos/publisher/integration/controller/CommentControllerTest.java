package dev.makos.publisher.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.makos.publisher.model.dto.CommentDTO;
import dev.makos.publisher.model.dto.exception.ErrorResponseDTO;
import dev.makos.publisher.model.entity.Comment;
import dev.makos.publisher.model.entity.Creator;
import dev.makos.publisher.model.entity.Tweet;
import dev.makos.publisher.repository.CommentRepository;
import dev.makos.publisher.repository.CreatorRepository;
import dev.makos.publisher.repository.TweetRepository;
import dev.makos.publisher.util.ErrorMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class CommentControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentRepository commentRepository;
    @Autowired
    private TweetRepository tweetRepository;
    @Autowired
    private CreatorRepository creatorRepository;
    private Tweet tweet;
    private Comment comment;

    @BeforeEach
    void beforeEach() {
        Creator creator = createCreator();
        tweet = createTweet(creator);

        comment = Comment.builder()
                .id(1L)
                .content("init-content")
                .tweet(tweet)
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .content("init-content-2")
                .tweet(tweet)
                .build();

        List<Comment> comments = List.of(comment, comment2);

        when(commentRepository.findAll()).thenReturn(comments);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.findById(not(eq(comment.getId())))).thenReturn(Optional.empty());
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentRepository.existsById(comment.getId())).thenReturn(true);
        when(commentRepository.existsById(not(eq(comment.getId())))).thenReturn(false);
    }

    @AfterEach
    void afterEach() {
        tweetRepository.deleteAll();
        creatorRepository.deleteAll();
    }

    @Test
    void saveOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(-1L);

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
        assertTrue(invalidFields.containsKey("content"));
        assertTrue(invalidFields.containsKey("tweetId"));
        assertTrue(invalidFields.containsKey("id"));
        assertEquals("Content is required", invalidFields.get("content"));
        assertEquals("Tweet ID is required", invalidFields.get("tweetId"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
    }

    @Test
    void saveOne_returnsBadRequest_whenTweetNotFound() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("content");
        commentDTO.setTweetId(100L);

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void saveOne_returnsCreated() throws Exception {
        // given
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent(comment.getContent());
        commentDTO.setTweetId(comment.getTweet().getId());

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

        assertNotNull(savedDTO.getId());
        assertEquals(commentDTO.getContent(), savedDTO.getContent());
        assertEquals(commentDTO.getTweetId(), savedDTO.getTweetId());

        verify(commentRepository).save(captor.capture());
        Comment savedEntity = captor.getValue();
        assertEquals(commentDTO.getContent(), savedEntity.getContent());
        assertEquals(commentDTO.getTweetId(), savedEntity.getTweet().getId());
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
    }

    @Test
    void getAll_returnsComments() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/comments"))
                .andExpect(status().isOk()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<CommentDTO> commentDTOs = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, CommentDTO.class));

        assertEquals(2, commentDTOs.size());
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
    void deleteOne_deletesComment() throws Exception {
        // given
        when(commentRepository.existsById(comment.getId())).thenReturn(true).thenReturn(false);

        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/comments/{id}", comment.getId()))
                .andExpect(status().isNoContent());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/comments/{id}", comment.getId()))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertFalse(commentRepository.existsById(comment.getId()));
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
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

        assertEquals(3, invalidFields.size());
        assertTrue(invalidFields.containsKey("content"));
        assertTrue(invalidFields.containsKey("tweetId"));
        assertTrue(invalidFields.containsKey("id"));
        assertEquals("Content is required", invalidFields.get("content"));
        assertEquals("Tweet ID is required", invalidFields.get("tweetId"));
        assertEquals("ID must be greater than 0", invalidFields.get("id"));
    }

    @Test
    void updateOne_returnsNotFound_whenCommentNotFound() throws Exception {
        // given
        Long notExistId = 100L;

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(notExistId);
        commentDTO.setContent("content");
        commentDTO.setTweetId(1L);

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsNotFound_whenTweetNotFound() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent("content");
        commentDTO.setTweetId(100L);

        String requestBody = objectMapper.writeValueAsString(commentDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_updatesComment() throws Exception {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent("new content");
        commentDTO.setTweetId(tweet.getId());

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

        assertEquals(commentDTO.getId(), updatedDTO.getId());
        assertEquals(commentDTO.getContent(), updatedDTO.getContent());
        assertEquals(commentDTO.getTweetId(), updatedDTO.getTweetId());
    }

    private Creator createCreator() {
        Creator creator = Creator.builder()
                .firstname("init-firstName")
                .lastname("init-second")
                .login("init-login")
                .password("init-password")
                .build();
        return creatorRepository.save(creator);
    }

    private Tweet createTweet(Creator creator) {
        Tweet tweet = Tweet.builder()
                .title("init-title")
                .content("init-content")
                .creator(creator)
                .build();
        return tweetRepository.save(tweet);
    }
}