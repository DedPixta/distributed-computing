package org.example.distributedcomputing.service;

import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.exception.CustomException;
import org.example.distributedcomputing.mapper.CreatorMapper;
import org.example.distributedcomputing.model.dto.CreatorDTO;
import org.example.distributedcomputing.model.entity.Creator;
import org.example.distributedcomputing.repository.CreatorRepository;
import org.example.distributedcomputing.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CreatorService {

    private final CreatorMapper creatorMapper;
    private final CreatorRepository creatorRepository;

    @Transactional
    public CreatorDTO saveOne(CreatorDTO creatorDTO) {
        if (creatorRepository.existsByLogin(creatorDTO.getLogin())) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message(ErrorMessage.CREATOR_LOGIN_ALREADY_EXISTS.getText())
                    .build();
        }

        Creator entity = creatorMapper.toEntity(creatorDTO);
        entity = creatorRepository.save(entity);
        return creatorMapper.toDTO(entity);
    }

    public CreatorDTO getOne(Long id) {
        return creatorRepository.findById(id)
                .map(creatorMapper::toDTO)
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.CREATOR_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    @Transactional
    public void deleteOne(Long id) {
        if (!creatorRepository.existsById(id)) {
            throw CustomException.builder()
                    .message(ErrorMessage.CREATOR_NOT_FOUND.getText())
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        creatorRepository.deleteById(id);
    }

    @Transactional
    public CreatorDTO updateOne(CreatorDTO creatorDTO) {
        Creator entity = creatorRepository.findById(creatorDTO.getId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.CREATOR_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        if (loginExists(creatorDTO)) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message(ErrorMessage.CREATOR_LOGIN_ALREADY_EXISTS.getText())
                    .build();
        }

        entity.setLogin(creatorDTO.getLogin());
        entity.setFirstname(creatorDTO.getFirstname());
        entity.setLastname(creatorDTO.getLastname());
        entity.setPassword(creatorDTO.getPassword());
        entity = creatorRepository.save(entity);
        return creatorMapper.toDTO(entity);
    }

    private boolean loginExists(CreatorDTO creatorDTO) {
        return creatorRepository.findCreatorByLogin(creatorDTO.getLogin())
                .filter(creator -> !creator.getId().equals(creatorDTO.getId()))
                .isPresent();
    }

    public List<CreatorDTO> getAll() {
        return creatorRepository.findAll().stream()
                .map(creatorMapper::toDTO)
                .toList();
    }
}
