package dev.makos.publisher.mapper;

import dev.makos.publisher.model.dto.TweetDTO;
import dev.makos.publisher.model.entity.Tweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "creatorId", source = "creator.id")
    TweetDTO toDTO(Tweet tweet);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "stickers", ignore = true)
    Tweet toEntity(TweetDTO tweetDTO);

}
