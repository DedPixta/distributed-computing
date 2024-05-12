package org.example.distributedcomputing.mapper;

import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.model.entity.Sticker;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TweetMapper.class})
public interface StickerMapper {

    StickerDTO toDTO(Sticker sticker);

    Sticker toEntity(StickerDTO stickerDTO);

}
