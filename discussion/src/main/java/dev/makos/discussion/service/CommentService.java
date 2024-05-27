package dev.makos.discussion.service;

import dev.makos.discussion.mapper.CommentMapper;
import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.entity.Comment;
import dev.makos.discussion.repository.CommentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentDTO save(CommentDTO commentDTO) {
        Comment entity = commentMapper.toEntity(commentDTO);
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }
}
