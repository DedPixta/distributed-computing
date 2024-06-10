package dev.makos.publisher.mapper;

import dev.makos.publisher.model.dto.CommentCassandraDTO;
import dev.makos.publisher.model.dto.CommentDTO;
import dev.makos.publisher.model.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "tweetId", source = "tweet.id")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "tweet", ignore = true)
    Comment toEntity(CommentDTO commentDTO);

    @Mapping(target = "country", ignore = true)
    CommentCassandraDTO toCassandraDTO(Comment comment);

    @Mapping(target = "tweet", ignore = true)
    Comment toEntity(CommentCassandraDTO remoteCommentDTO);
}
