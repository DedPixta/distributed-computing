package dev.makos.discussion.service;

import dev.makos.discussion.exception.CustomException;
import dev.makos.discussion.mapper.CommentMapper;
import dev.makos.discussion.mapper.CommentMapperImpl;
import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.entity.Comment;
import dev.makos.discussion.model.entity.CommentKey;
import dev.makos.discussion.repository.CommentRepository;
import dev.makos.discussion.repository.IdRepository;
import dev.makos.discussion.util.ErrorMessage;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final String COMMENT_ID_COUNTER = "comment_id";
    private static final String COUNTRY = "KZ";

    @Spy
    private CommentMapper commentMapper = new CommentMapperImpl();

    @Mock
    private IdRepository idRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService underTest;

    @Test
    void saveOne_returnsCommentDTO() {
        // given
        ArgumentCaptor<Comment> capture = ArgumentCaptor.forClass(Comment.class);

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCountry(COUNTRY);
        commentDTO.setTweetId(10L);
        commentDTO.setContent("content");

        long commentId = 1L;
        Comment comment = new Comment();
        CommentKey commentKey = new CommentKey(commentDTO.getCountry(), commentDTO.getTweetId(), commentId);
        comment.setKey(commentKey);

        CommentDTO expected = commentMapper.toDTO(comment);

        when(idRepository.getCurrentId(COMMENT_ID_COUNTER)).thenReturn(commentId);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        // when
        CommentDTO actual = underTest.saveOne(commentDTO);
        // then
        assertEquals(expected, actual);

        verify(idRepository).getCurrentId(COMMENT_ID_COUNTER);
        verify(idRepository).increment(COMMENT_ID_COUNTER);

        verify(commentRepository).save(capture.capture());
        Comment savedComment = capture.getValue();
        assertEquals(commentDTO.getCountry(), savedComment.getKey().getCountry());
        assertEquals(commentDTO.getTweetId(), savedComment.getKey().getTweetId());
        assertEquals(commentDTO.getContent(), savedComment.getContent());
    }

    @Test
    void getOne_throwException_whenCommentNotFound() {
        // given
        long commentId = 1L;
        when(commentRepository.findCommentById(commentId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.getOne(commentId));
        // then
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(commentRepository).findCommentById(commentId);
    }


    @Test
    void getOne_returnsCommentDTO() {
        // given
        CommentDTO expected = new CommentDTO();
        expected.setId(1L);
        expected.setCountry(COUNTRY);
        expected.setTweetId(10L);
        expected.setContent("content");

        Comment comment = new Comment();
        CommentKey commentKey = new CommentKey(expected.getCountry(), expected.getTweetId(), expected.getId());
        comment.setKey(commentKey);
        comment.setContent(expected.getContent());

        when(commentRepository.findCommentById(expected.getId())).thenReturn(Optional.of(comment));
        // when
        CommentDTO actual = underTest.getOne(expected.getId());
        // then
        assertEquals(expected, actual);
        verify(commentRepository).findCommentById(expected.getId());
    }

    @Test
    void deleteOne_throwException_whenCommentNotFound() {
        // given
        long commentId = 1L;
        when(commentRepository.findCommentById(commentId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deleteOne(commentId));
        // then
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(commentRepository).findCommentById(commentId);
    }

    @Test
    void deleteOne_deletesComment() {
        // given
        long commentId = 1L;
        Comment comment = new Comment();
        CommentKey commentKey = new CommentKey(COUNTRY, 10L, commentId);
        comment.setKey(commentKey);

        when(commentRepository.findCommentById(commentId)).thenReturn(Optional.of(comment));
        // when
        underTest.deleteOne(commentId);
        // then
        verify(commentRepository).deleteById(commentKey);
    }

    @Test
    void updateOne_throwException_whenCommentNotFound() {
        // given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        when(commentRepository.findCommentById(commentDTO.getId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(commentDTO));
        // then
        assertEquals(ErrorMessage.COMMENT_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(commentRepository).findCommentById(commentDTO.getId());
    }

    @Test
    void updateOne_returnsCommentDTO() {
        // given
        CommentDTO expected = new CommentDTO();
        expected.setId(1L);
        expected.setCountry(COUNTRY);
        expected.setTweetId(10L);
        expected.setContent("new content");

        CommentKey commentKey = new CommentKey(COUNTRY, expected.getTweetId(), expected.getId());

        Comment saveComment = new Comment();
        saveComment.setKey(commentKey);
        saveComment.setContent("old content");

        Comment updatedComment = new Comment();
        updatedComment.setKey(commentKey);
        updatedComment.setContent(expected.getContent());

        when(commentRepository.findCommentById(expected.getId())).thenReturn(Optional.of(saveComment));
        when(commentRepository.save(saveComment)).thenReturn(updatedComment);
        // when
        CommentDTO actual = underTest.updateOne(expected);
        // then
        assertEquals(expected, actual);

        verify(commentRepository).findCommentById(expected.getId());
        verify(commentRepository).save(saveComment);
    }

    @Test
    void getAll_returnsCommentsDTO() {
        // given
        List<Comment> comments = Instancio.ofList(Comment.class).size(2).create();
        List<CommentDTO> expected = comments.stream()
                .map(commentMapper::toDTO)
                .toList();

        when(commentRepository.findAll()).thenReturn(comments);
        // when
        List<CommentDTO> actual = underTest.getAll();
        // then
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);

        verify(commentRepository).findAll();
    }
}