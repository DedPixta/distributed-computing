package org.example.distributedcomputing.service;

import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.mapper.CreatorMapper;
import org.example.distributedcomputing.model.dto.CreatorDTO;
import org.example.distributedcomputing.model.entity.Creator;
import org.example.distributedcomputing.repository.CreatorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CreatorService {

    private final CreatorMapper creatorMapper;
    private final CreatorRepository creatorRepository;

    @Transactional
    public CreatorDTO saveOne(CreatorDTO creatorDTO) {
        Creator entity = creatorMapper.toEntity(creatorDTO);
        entity = creatorRepository.save(entity);
        return creatorMapper.toDTO(entity);
    }

    public CreatorDTO getOne(Long id) {
        Creator entity = creatorRepository.findById(id).orElseThrow();
        return creatorMapper.toDTO(entity);
    }

    @Transactional
    public void deleteOne(Long id) {
        creatorRepository.deleteById(id);
    }

    @Transactional
    public CreatorDTO updateOne(Long id, CreatorDTO creatorDTO) {
        Creator entity = creatorRepository.findById(id).orElseThrow();
        entity.setLogin(creatorDTO.getLogin());
        entity.setFirstname(creatorDTO.getFirstname());
        entity.setLastname(creatorDTO.getLastname());
        entity = creatorRepository.save(entity);
        return creatorMapper.toDTO(entity);
    }

    public List<CreatorDTO> getAll() {
        return creatorMapper.toDTOs(creatorRepository.findAll());
    }
}
