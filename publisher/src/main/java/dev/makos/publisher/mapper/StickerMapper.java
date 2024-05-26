package dev.makos.publisher.mapper;

import dev.makos.publisher.model.dto.StickerDTO;
import dev.makos.publisher.model.entity.Sticker;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TweetMapper.class})
public interface StickerMapper {

    StickerDTO toDTO(Sticker sticker);

    Sticker toEntity(StickerDTO stickerDTO);

}
