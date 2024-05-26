package dev.makos.publisher.service;

import dev.makos.publisher.exception.CustomException;
import dev.makos.publisher.mapper.StickerMapper;
import dev.makos.publisher.model.dto.StickerDTO;
import dev.makos.publisher.model.entity.Sticker;
import dev.makos.publisher.repository.StickerRepository;
import dev.makos.publisher.util.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StickerService {

    private final StickerMapper stickerMapper;
    private final StickerRepository stickerRepository;

    @Transactional
    public StickerDTO saveOne(StickerDTO stickerDTO) {
        Sticker entity = stickerMapper.toEntity(stickerDTO);
        entity = stickerRepository.save(entity);
        return stickerMapper.toDTO(entity);
    }

    public StickerDTO getOne(Long id) {
        return stickerRepository.findById(id)
                .map(stickerMapper::toDTO)
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.STICKER_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    @Transactional
    public void deleteOne(Long id) {
        if (!stickerRepository.existsById(id)) {
            throw CustomException.builder()
                    .message(ErrorMessage.STICKER_NOT_FOUND.getText())
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        stickerRepository.deleteById(id);
    }

    @Transactional
    public StickerDTO updateOne(StickerDTO stickerDTO) {
        Sticker entity = stickerRepository.findById(stickerDTO.getId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.STICKER_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        entity.setName(stickerDTO.getName());
        entity = stickerRepository.save(entity);
        return stickerMapper.toDTO(entity);
    }

    public List<StickerDTO> getAll() {
        return stickerRepository.findAll().stream()
                .map(stickerMapper::toDTO)
                .toList();
    }
}
