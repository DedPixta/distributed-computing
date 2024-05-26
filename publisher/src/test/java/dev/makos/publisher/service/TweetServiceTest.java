package dev.makos.publisher.service;

import dev.makos.publisher.exception.CustomException;
import dev.makos.publisher.mapper.TweetMapper;
import dev.makos.publisher.mapper.TweetMapperImpl;
import dev.makos.publisher.model.dto.TweetDTO;
import dev.makos.publisher.model.entity.Creator;
import dev.makos.publisher.model.entity.Tweet;
import dev.makos.publisher.repository.CreatorRepository;
import dev.makos.publisher.repository.TweetRepository;
import dev.makos.publisher.util.ErrorMessage;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetServiceTest {

    @Spy
    private TweetMapper tweetMapper = new TweetMapperImpl();

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private CreatorRepository creatorRepository;

    @InjectMocks
    private TweetService underTest;


    @DisplayName("Save one tweet when not found")
    @Test
    void saveOne_throwException_whenCreatorNotFound() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);

        when(creatorRepository.findById(tweetDTO.getCreatorId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.saveOne(tweetDTO));
        // then
        assertEquals(ErrorMessage.CREATOR_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(tweetRepository, never()).save(any());
    }

    @DisplayName("Save one tweet when title exists")
    @Test
    void saveOne_throwException_whenTitleExists() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        Creator creator = Instancio.create(Creator.class);

        when(creatorRepository.findById(tweetDTO.getCreatorId())).thenReturn(Optional.of(creator));
        when(tweetRepository.existsByTitle(tweetDTO.getTitle())).thenReturn(true);
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.saveOne(tweetDTO));
        // then
        assertEquals(ErrorMessage.TWEET_TITLE_ALREADY_EXISTS.getText(), exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

        verify(tweetRepository, never()).save(any());
    }

    @DisplayName("Save one tweet successfully")
    @Test
    void saveOne_returnTweetDTO() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        Creator creator = Instancio.create(Creator.class);
        Tweet tweet = tweetMapper.toEntity(tweetDTO);
        tweet.setCreator(creator);

        when(creatorRepository.findById(tweetDTO.getCreatorId())).thenReturn(Optional.of(creator));
        when(tweetRepository.existsByTitle(tweetDTO.getTitle())).thenReturn(false);
        when(tweetRepository.save(any())).thenReturn(tweet);
        // when
        TweetDTO result = underTest.saveOne(tweetDTO);
        // then
        assertNotNull(result);
        assertEquals(tweet.getId(), result.getId());
        assertEquals(tweet.getTitle(), result.getTitle());
    }

    @DisplayName("Get one tweet not found")
    @Test
    void getOne_throwException_whenTweetNotFound() {
        // given
        Long id = 1L;
        when(tweetRepository.findById(id)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.getOne(id));
        // then
        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @DisplayName("Get one tweet successfully")
    @Test
    void getOne_returnTweetDTO() {
        // given
        Long id = 1L;
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        Tweet tweet = tweetMapper.toEntity(tweetDTO);
        tweet.setId(id);

        when(tweetRepository.findById(id)).thenReturn(Optional.of(tweet));
        // when
        TweetDTO result = underTest.getOne(tweet.getId());
        // then
        assertNotNull(result);
        assertEquals(tweet.getId(), result.getId());
        assertEquals(tweet.getTitle(), result.getTitle());
    }

    @DisplayName("Delete one tweet successfully")
    @Test
    void deleteOne() {
        // given
        Long id = 1L;
        // when
        underTest.deleteOne(id);
        // then
        verify(tweetRepository).deleteById(id);
    }

    @DisplayName("Update one tweet when not found")
    @Test
    void updateOne_throwException_whenTweetNotFound() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);

        when(tweetRepository.findById(tweetDTO.getId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(tweetDTO));
        // then
        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(tweetRepository, never()).save(any());
    }

    @DisplayName("Update one tweet when title exists")
    @Test
    void updateOne_throwException_whenTitleExists() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        tweetDTO.setId(1L);
        Tweet tweet = Instancio.create(Tweet.class);
        tweet.setId(2L);
        tweet.setTitle(tweetDTO.getTitle());

        when(tweetRepository.findById(tweetDTO.getId())).thenReturn(Optional.of(tweet));
        when(tweetRepository.findTweetByTitle(tweetDTO.getTitle())).thenReturn(Optional.of(tweet));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(tweetDTO));
        // then
        assertEquals(ErrorMessage.TWEET_TITLE_ALREADY_EXISTS.getText(), exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

        verify(tweetRepository, never()).save(any());
    }

    @DisplayName("Update one tweet with different creator not found")
    @Test
    void updateOne_throwException_whenCreatorNotFound() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        tweetDTO.setCreatorId(1L);
        Tweet tweet = Instancio.create(Tweet.class);
        tweet.setId(tweetDTO.getId());
        Creator creator = tweet.getCreator();
        creator.setId(2L);

        when(tweetRepository.findById(tweetDTO.getId())).thenReturn(Optional.of(tweet));
        when(tweetRepository.findTweetByTitle(tweetDTO.getTitle())).thenReturn(Optional.empty());
        when(creatorRepository.findById(tweetDTO.getCreatorId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(tweetDTO));
        // then
        assertEquals(ErrorMessage.CREATOR_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(tweetRepository, never()).save(any());
    }

    @DisplayName("Update one tweet with different creator")
    @Test
    void updateOne_returnTweetDTO_whenDifferentCreator() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        Tweet tweet = Instancio.create(Tweet.class);
        tweet.setId(tweetDTO.getId());
        Creator creator = tweet.getCreator();
        creator.setId(1L);

        Creator creator2 = Instancio.create(Creator.class);

        when(tweetRepository.findById(tweetDTO.getId())).thenReturn(Optional.of(tweet));
        when(tweetRepository.findTweetByTitle(tweetDTO.getTitle())).thenReturn(Optional.empty());
        when(creatorRepository.findById(tweetDTO.getCreatorId())).thenReturn(Optional.of(creator2));
        when(tweetRepository.save(any())).thenReturn(tweet);
        // when
        TweetDTO result = underTest.updateOne(tweetDTO);
        // then
        assertNotNull(result);
        assertEquals(tweetDTO.getId(), result.getId());
        assertEquals(tweetDTO.getTitle(), result.getTitle());
        assertEquals(creator2.getId(), result.getCreatorId());
        assertEquals(tweetDTO.getContent(), result.getContent());
    }

    @DisplayName("Update one tweet successfully")
    @Test
    void updateOne_returnTweetDTO() {
        // given
        TweetDTO tweetDTO = Instancio.create(TweetDTO.class);
        Tweet tweet = Instancio.create(Tweet.class);
        Creator creator = tweet.getCreator();
        creator.setId(tweetDTO.getCreatorId());

        when(tweetRepository.findById(tweetDTO.getId())).thenReturn(Optional.of(tweet));
        when(tweetRepository.findTweetByTitle(tweetDTO.getTitle())).thenReturn(Optional.empty());
        when(tweetRepository.save(any())).thenReturn(tweet);
        // when
        TweetDTO result = underTest.updateOne(tweetDTO);
        // then
        assertNotNull(result);
        assertEquals(tweet.getId(), result.getId());
        assertEquals(tweet.getTitle(), result.getTitle());
    }

    @DisplayName("Get all tweets")
    @Test
    void getAll_returnListOfTweetDTO() {
        // given
        List<Tweet> tweets = Instancio.ofList(Tweet.class).size(2).create();
        List<TweetDTO> expected = tweets.stream()
                .map(tweetMapper::toDTO)
                .toList();

        when(tweetRepository.findAll()).thenReturn(tweets);
        // when
        List<TweetDTO> result = underTest.getAll();
        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }
}