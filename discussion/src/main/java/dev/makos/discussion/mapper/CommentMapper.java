package dev.makos.discussion.mapper;

import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.model.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "country", source = "key.country")
    @Mapping(target = "id", source = "key.id")
    @Mapping(target = "tweetId", source = "key.tweetId")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "key.country", source = "country")
    @Mapping(target = "key.tweetId", source = "tweetId")
    @Mapping(target = "key.id", source = "id")
    Comment toEntity(CommentDTO commentDTO);

}
