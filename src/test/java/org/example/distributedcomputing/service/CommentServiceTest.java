package org.example.distributedcomputing.service;

import org.example.distributedcomputing.exception.CustomException;
import org.example.distributedcomputing.mapper.CommentMapper;
import org.example.distributedcomputing.mapper.CommentMapperImpl;
import org.example.distributedcomputing.model.dto.CommentDTO;
import org.example.distributedcomputing.model.entity.Comment;
import org.example.distributedcomputing.model.entity.Tweet;
import org.example.distributedcomputing.repository.CommentRepository;
import org.example.distributedcomputing.repository.TweetRepository;
import org.example.distributedcomputing.util.ErrorMessage;
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
class CommentServiceTest {

    @Spy
    private CommentMapper commentMapper = new CommentMapperImpl();

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TweetRepository tweetRepository;

    @InjectMocks
    private CommentService underTest;

    @DisplayName("Save one comment with no tweet found")
    @Test
    void saveOne_throwException_whenTweetNotFound() {
        // given
        CommentDTO commentDTO = Instancio.create(CommentDTO.class);

        when(tweetRepository.findById(commentDTO.getTweetId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.saveOne(commentDTO));
        // then
        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(commentRepository, never()).save(any());
    }

    @DisplayName("Save one comment successfully")
    @Test
    void saveOne_saveComment_whenTweetFound() {
        // given
        CommentDTO commentDTO = Instancio.create(CommentDTO.class);
        Tweet tweet = Instancio.create(Tweet.class);
        tweet.setId(commentDTO.getTweetId());

        Long expectedID = 1L;
        Comment entity = commentMapper.toEntity(commentDTO);
        entity.setId(expectedID);
        entity.setTweet(tweet);

        when(tweetRepository.findById(commentDTO.getTweetId())).thenReturn(Optional.of(tweet));
        when(commentRepository.save(any())).thenReturn(entity);
        // when
        CommentDTO result = underTest.saveOne(commentDTO);
        // then
        assertNotNull(result);
        assertEquals(expectedID, result.getId());
        assertEquals(commentDTO.getTweetId(), result.getTweetId());
        assertEquals(commentDTO.getContent(), result.getContent());
    }

    @DisplayName("Get one comment with no comment found")
    @Test
    void getOne_throwException_whenCommentNotFound() {
        // given
        Long id = 1L;

        when(commentRepository.findById(id)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.getOne(id));
        // then
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @DisplayName("Get one comment successfully")
    @Test
    void getOne_returnComment_whenCommentFound() {
        // given
        Long id = 1L;
        Comment entity = Instancio.create(Comment.class);
        entity.setId(id);
        CommentDTO expected = commentMapper.toDTO(entity);

        when(commentRepository.findById(id)).thenReturn(Optional.of(entity));
        // when
        CommentDTO result = underTest.getOne(id);
        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @DisplayName("Delete one comment")
    @Test
    void deleteOne_deleteComment() {
        // given
        Long id = 1L;
        // when
        underTest.deleteOne(id);
        // then
        verify(commentRepository).deleteById(id);
    }

    @DisplayName("Update one comment with different tweet")
    @Test
    void updateOne_throwException_whenTweetNotFound() {
        // given
        CommentDTO commentDTO = Instancio.create(CommentDTO.class);
        commentDTO.setTweetId(1L);
        Comment comment = Instancio.create(Comment.class);
        comment.getTweet().setId(2L);

        when(commentRepository.findById(commentDTO.getId())).thenReturn(Optional.of(comment));
        when(tweetRepository.findById(commentDTO.getTweetId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(commentDTO));
        // then
        assertEquals(ErrorMessage.TWEET_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(commentRepository, never()).save(any());
    }

    @DisplayName("Update one comment with no comment found")
    @Test
    void updateOne_throwException_whenCommentNotFound() {
        // given
        CommentDTO commentDTO = Instancio.create(CommentDTO.class);

        when(commentRepository.findById(commentDTO.getId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(commentDTO));
        // then
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(commentRepository, never()).save(any());
    }

    @DisplayName("Update one comment with different tweet")
    @Test
    void updateOne_updateComment_whenDifferentTweet() {
        // given
        CommentDTO commentDTO = Instancio.create(CommentDTO.class);
        Comment entity = Instancio.create(Comment.class);
        entity.setId(commentDTO.getId());
        entity.setTweet(Instancio.create(Tweet.class));
        entity.getTweet().setId(1L);
        entity.setContent("old content");

        Tweet tweet = Instancio.create(Tweet.class);
        tweet.setId(2L);

        when(commentRepository.findById(commentDTO.getId())).thenReturn(Optional.of(entity));
        when(tweetRepository.findById(commentDTO.getTweetId())).thenReturn(Optional.of(tweet));
        when(commentRepository.save(any())).thenReturn(entity);
        // when
        CommentDTO result = underTest.updateOne(commentDTO);
        // then
        assertNotNull(result);
        assertEquals(commentDTO.getId(), result.getId());
        assertEquals(tweet.getId(), result.getTweetId());
        assertEquals(commentDTO.getContent(), result.getContent());
    }

    @DisplayName("Update one comment with same tweet")
    @Test
    void updateOne_updateComment_whenSameTweet() {
        // given
        CommentDTO commentDTO = Instancio.create(CommentDTO.class);
        Comment entity = Instancio.create(Comment.class);
        entity.setId(commentDTO.getId());
        entity.setTweet(Instancio.create(Tweet.class));
        entity.getTweet().setId(commentDTO.getTweetId());
        entity.setContent("old content");

        when(commentRepository.findById(commentDTO.getId())).thenReturn(Optional.of(entity));
        when(commentRepository.save(any())).thenReturn(entity);
        // when
        CommentDTO result = underTest.updateOne(commentDTO);
        // then
        assertNotNull(result);
        assertEquals(commentDTO.getId(), result.getId());
        assertEquals(commentDTO.getTweetId(), result.getTweetId());
        assertEquals(commentDTO.getContent(), result.getContent());

        verify(tweetRepository, never()).findById(any());
    }

    @DisplayName("Get all comments")
    @Test
    void getAll_returnAllComments() {
        // given
        List<Comment> comments = Instancio.ofList(Comment.class).size(2).create();
        List<CommentDTO> expected = comments.stream().map(commentMapper::toDTO).toList();

        when(commentRepository.findAll()).thenReturn(comments);
        // when
        List<CommentDTO> result = underTest.getAll();
        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }
}