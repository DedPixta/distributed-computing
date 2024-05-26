package dev.makos.publisher.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.makos.publisher.integration.config.SpringBootTestContainers;
import dev.makos.publisher.model.dto.CommentDTO;
import dev.makos.publisher.model.dto.TweetDTO;
import dev.makos.publisher.model.dto.exception.ErrorResponseDTO;
import dev.makos.publisher.model.entity.Creator;
import dev.makos.publisher.model.entity.Tweet;
import dev.makos.publisher.repository.CreatorRepository;
import dev.makos.publisher.repository.TweetRepository;
import dev.makos.publisher.util.ErrorMessage;
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
class TweetControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TweetRepository tweetRepository;
    @Autowired
    private CreatorRepository creatorRepository;

    private Tweet tweet;
    private Creator creator;

    @BeforeEach
    void beforeEach() {
        creator = createCreator();
        tweet = createTweet(creator);
    }

    @AfterEach
    void afterEach() {
        tweetRepository.deleteAll();
        creatorRepository.deleteAll();
    }

    @Test
    void saveOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(-1L);
        tweetDTO.setTitle("t");
        tweetDTO.setContent("c");
        tweetDTO.setCreatorId(-1L);

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/tweets")
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
        assertTrue(invalidFields.containsKey("title"));
        assertTrue(invalidFields.containsKey("content"));
        assertTrue(invalidFields.containsKey("creatorId"));
        assertEquals("Tweet ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Title must be between 2 and 64 characters", invalidFields.get("title"));
        assertEquals("Content must be between 4 and 2048 characters", invalidFields.get("content"));
        assertEquals("Creator ID must be greater than 0", invalidFields.get("creatorId"));
    }

    @Test
    void saveOne_returnsNotFound_whenCreatorNotFound() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setTitle("title");
        tweetDTO.setContent("content");
        tweetDTO.setCreatorId(100L);

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.CREATOR_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void saveOne_returnsForbidden_whenTitleAlreadyExists() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setTitle(tweet.getTitle());
        tweetDTO.setContent("content");
        tweetDTO.setCreatorId(creator.getId());

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_TITLE_ALREADY_EXISTS.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponseDTO.getCode());
    }

    @Test
    void saveOne_returnsCreated() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setTitle("title");
        tweetDTO.setContent("content");
        tweetDTO.setCreatorId(creator.getId());

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        TweetDTO responseTweetDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), TweetDTO.class);

        assertNotNull(responseTweetDTO.getId());
        assertEquals(tweetDTO.getTitle(), responseTweetDTO.getTitle());
        assertEquals(tweetDTO.getContent(), responseTweetDTO.getContent());
        assertEquals(tweetDTO.getCreatorId(), responseTweetDTO.getCreatorId());
    }

    @Test
    void getOne_returnsNotFound_whenTweetNotFound() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/tweets/{id}", 100L))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void getOne_returnsTweet() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/tweets/{id}", tweet.getId()))
                .andExpect(status().isOk()).andReturn();
        // then
        TweetDTO responseTweetDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), TweetDTO.class);

        assertEquals(tweet.getId(), responseTweetDTO.getId());
        assertEquals(tweet.getTitle(), responseTweetDTO.getTitle());
        assertEquals(tweet.getContent(), responseTweetDTO.getContent());
        assertEquals(tweet.getCreator().getId(), responseTweetDTO.getCreatorId());
    }

    @Test
    void getAll_returnsTweets() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/tweets"))
                .andExpect(status().isOk()).andReturn();
        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<CommentDTO> commentDTOs = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, TweetDTO.class));


        assertEquals(1, commentDTOs.size());
    }

    @Test
    void deleteOne_returnsNotFound_whenTweetNotFound() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/tweets/{id}", 100L))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void deleteOne_deletesTweet() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1.0/tweets/{id}", tweet.getId()))
                .andExpect(status().isNoContent());
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1.0/tweets/{id}", tweet.getId()))
                .andExpect(status().isNotFound()).andReturn();

        // then
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(contentAsString, ErrorResponseDTO.class);

        assertFalse(tweetRepository.existsById(tweet.getId()));
        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsBadRequest_whenValidationFailed() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(-1L);
        tweetDTO.setTitle("t");
        tweetDTO.setContent("c");
        tweetDTO.setCreatorId(-1L);

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/tweets")
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
        assertTrue(invalidFields.containsKey("title"));
        assertTrue(invalidFields.containsKey("content"));
        assertTrue(invalidFields.containsKey("creatorId"));
        assertEquals("Tweet ID must be greater than 0", invalidFields.get("id"));
        assertEquals("Title must be between 2 and 64 characters", invalidFields.get("title"));
        assertEquals("Content must be between 4 and 2048 characters", invalidFields.get("content"));
        assertEquals("Creator ID must be greater than 0", invalidFields.get("creatorId"));
    }

    @Test
    void updateOne_returnsNotFound_whenTweetNotFound() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(100L);
        tweetDTO.setTitle("updated-title");
        tweetDTO.setContent("updated-content");
        tweetDTO.setCreatorId(creator.getId());

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsNotFound_whenCreatorNotFound() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(tweet.getId());
        tweetDTO.setTitle("updated-title");
        tweetDTO.setContent("updated-content");
        tweetDTO.setCreatorId(100L);

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.CREATOR_NOT_FOUND.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsForbidden_whenTitleAlreadyExists() throws Exception {
        // given
        Tweet tweet2 = Tweet.builder()
                .title("existing-title")
                .content("content2")
                .creator(creator)
                .build();

        tweetRepository.save(tweet2);

        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(tweet2.getId());
        tweetDTO.setTitle(tweet.getTitle());
        tweetDTO.setContent("updated-content");
        tweetDTO.setCreatorId(creator.getId());

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), ErrorResponseDTO.class);

        assertEquals(ErrorMessage.TWEET_TITLE_ALREADY_EXISTS.getText(), errorResponseDTO.getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponseDTO.getCode());
    }

    @Test
    void updateOne_returnsUpdatedTweet() throws Exception {
        // given
        TweetDTO tweetDTO = new TweetDTO();
        tweetDTO.setId(tweet.getId());
        tweetDTO.setTitle("updated-title");
        tweetDTO.setContent("updated-content");
        tweetDTO.setCreatorId(creator.getId());

        String requestBody = objectMapper.writeValueAsString(tweetDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1.0/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        TweetDTO responseTweetDTO = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(StandardCharsets.UTF_8), TweetDTO.class);

        assertEquals(tweetDTO.getId(), responseTweetDTO.getId());
        assertEquals(tweetDTO.getTitle(), responseTweetDTO.getTitle());
        assertEquals(tweetDTO.getContent(), responseTweetDTO.getContent());
        assertEquals(tweetDTO.getCreatorId(), responseTweetDTO.getCreatorId());
    }

    private Tweet createTweet(Creator creator) {
        Tweet tweet = Tweet.builder()
                .title("init-title")
                .content("init-content")
                .creator(creator)
                .build();
        return tweetRepository.save(tweet);
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
