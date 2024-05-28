package dev.makos.discussion.service;

import dev.makos.discussion.exception.CustomException;
import dev.makos.discussion.mapper.CommentMapper;
import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.entity.Comment;
import dev.makos.discussion.repository.CommentRepository;
import dev.makos.discussion.util.ErrorMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    public CommentDTO saveOne(CommentDTO commentDTO) {
        Comment entity = commentMapper.toEntity(commentDTO);
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }

    public CommentDTO getOne(Long id) {
        return commentRepository.findById(id)
                .map(commentMapper::toDTO)
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    public void deleteOne(Long id) {
        if (!commentRepository.existsById(id)) {
            throw CustomException.builder()
                    .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        commentRepository.deleteById(id);
    }

    public CommentDTO updateOne(CommentDTO commentDTO) {
        Comment entity = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        if (!entity.getTweetId().equals(commentDTO.getTweetId())) {
            throw  CustomException.builder()
                            .message(ErrorMessage.TWEET_NOT_FOUND.getText())
                            .httpStatus(HttpStatus.NOT_FOUND)
                            .build());
            entity.setTweet(tweet);
        }

        entity.setContent(commentDTO.getContent());
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }

    public List<CommentDTO> getAll() {
        return commentRepository.findAll().stream()
                .map(commentMapper::toDTO)
                .toList();
    }
}
