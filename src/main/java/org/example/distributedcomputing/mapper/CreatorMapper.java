package org.example.distributedcomputing.mapper;

import org.example.distributedcomputing.model.dto.CreatorDTO;
import org.example.distributedcomputing.model.entity.Creator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreatorMapper {

    CreatorDTO toDTO(Creator creator);

    Creator toEntity(CreatorDTO creatorDTO);

}
