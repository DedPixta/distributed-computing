package org.example.distributedcomputing.mapper;

import org.example.distributedcomputing.model.dto.CommentDTO;
import org.example.distributedcomputing.model.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "tweetId", source = "tweet.id")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "tweet", ignore = true)
    Comment toEntity(CommentDTO commentDTO);

}
