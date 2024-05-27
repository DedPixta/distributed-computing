package dev.makos.discussion.mapper;

import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDTO toDTO(Comment comment);

    Comment toEntity(CommentDTO commentDTO);

}
