package org.example.distributedcomputing.mapper;

import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.model.entity.Sticker;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StickerMapper {

    StickerDTO toDTO(Sticker sticker);

    Sticker toEntity(StickerDTO stickerDTO);

    List<StickerDTO> toDTOs(List<Sticker> stickers);

}
