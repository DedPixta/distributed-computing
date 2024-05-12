package org.example.distributedcomputing.service;

import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.exception.CustomException;
import org.example.distributedcomputing.mapper.StickerMapper;
import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.model.entity.Sticker;
import org.example.distributedcomputing.repository.StickerRepository;
import org.example.distributedcomputing.util.ErrorMessage;
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
