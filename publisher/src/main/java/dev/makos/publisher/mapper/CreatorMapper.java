package dev.makos.publisher.mapper;

import dev.makos.publisher.model.dto.CreatorDTO;
import dev.makos.publisher.model.entity.Creator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreatorMapper {

    CreatorDTO toDTO(Creator creator);

    Creator toEntity(CreatorDTO creatorDTO);

}
