package org.example.distributedcomputing.service;

import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.mapper.StickerMapper;
import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.model.entity.Sticker;
import org.example.distributedcomputing.repository.StickerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
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
        Sticker entity = stickerRepository.findById(id).orElseThrow();
        return stickerMapper.toDTO(entity);
    }

    @Transactional
    public void deleteOne(Long id) {
        stickerRepository.deleteById(id);
    }

    @Transactional
    public StickerDTO updateOne(Long id, StickerDTO stickerDTO) {
        Sticker entity = stickerRepository.findById(id).orElseThrow();
        entity.setName(stickerDTO.getName());
        entity = stickerRepository.save(entity);
        return stickerMapper.toDTO(entity);
    }

    public List<StickerDTO> getAll() {
        return stickerMapper.toDTOs(stickerRepository.findAll());
    }
}
