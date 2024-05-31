package dev.makos.discussion.service;

import dev.makos.discussion.exception.CustomException;
import dev.makos.discussion.mapper.CommentMapper;
import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.entity.Comment;
import dev.makos.discussion.repository.CommentRepository;
import dev.makos.discussion.repository.IdRepository;
import dev.makos.discussion.util.ErrorMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CommentService {

    private static final String COMMENT_ID = "comment_id";
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final IdRepository idRepository;

    public CommentDTO saveOne(CommentDTO commentDTO) {
        Comment entity = commentMapper.toEntity(commentDTO);
        long id = getNextCommentId();
        entity.setId(id);
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }

    public CommentDTO getOne(Long id) {
        return commentRepository.findCommentById(id)
                .map(commentMapper::toDTO)
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    public void deleteOne(Long id) {
        Optional<Comment> commentById = commentRepository.findCommentById(id);
        if (commentById.isEmpty()) {
            throw CustomException.builder()
                    .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        commentRepository.deleteById(commentById.get().getKey());
    }

    public CommentDTO updateOne(CommentDTO commentDTO) {
        Comment entity = commentRepository.findCommentById(commentDTO.getId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        entity.setContent(commentDTO.getContent());
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }

    public List<CommentDTO> getAll() {
        return commentRepository.findAll().stream()
                .map(commentMapper::toDTO)
                .toList();
    }

    private synchronized long getNextCommentId() {
        idRepository.increment(COMMENT_ID);
        return idRepository.getCurrentId(COMMENT_ID);
    }
}
