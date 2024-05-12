package org.example.distributedcomputing.service;

import org.example.distributedcomputing.exception.CustomException;
import org.example.distributedcomputing.mapper.CreatorMapper;
import org.example.distributedcomputing.mapper.CreatorMapperImpl;
import org.example.distributedcomputing.model.dto.CreatorDTO;
import org.example.distributedcomputing.model.entity.Creator;
import org.example.distributedcomputing.repository.CreatorRepository;
import org.example.distributedcomputing.util.ErrorMessage;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatorServiceTest {

    @Spy
    private CreatorMapper creatorMapper = new CreatorMapperImpl();

    @Mock
    private CreatorRepository creatorRepository;

    @InjectMocks
    private CreatorService underTest;

    @DisplayName("Save one creator with existing login")
    @Test
    void saveOne_throwException_whenLoginExists() {
        // given
        CreatorDTO creatorDTO = Instancio.create(CreatorDTO.class);

        when(creatorRepository.existsByLogin(creatorDTO.getLogin())).thenReturn(true);
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.saveOne(creatorDTO));
        // then
        assertEquals(ErrorMessage.CREATOR_LOGIN_ALREADY_EXISTS.getText(), exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

        verify(creatorRepository, never()).save(any());
    }

    @DisplayName("Save one creator successfully")
    @Test
    void saveOne_returnCreatorDTO_whenLoginNotExists() {
        // given
        CreatorDTO creatorDTO = Instancio.create(CreatorDTO.class);
        Creator entity = creatorMapper.toEntity(creatorDTO);
        entity.setId(1L);

        when(creatorRepository.existsByLogin(creatorDTO.getLogin())).thenReturn(false);
        when(creatorRepository.save(any())).thenReturn(entity);
        // when
        CreatorDTO result = underTest.saveOne(creatorDTO);
        // then
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getLogin(), result.getLogin());
        assertEquals(entity.getFirstname(), result.getFirstname());
        assertEquals(entity.getLastname(), result.getLastname());
        assertEquals(entity.getPassword(), result.getPassword());

        verify(creatorRepository).save(any());
    }

    @DisplayName("Get one creator successfully")
    @Test
    void getOne_returnCreatorDTO_whenCreatorExists() {
        // given
        CreatorDTO creatorDTO = Instancio.create(CreatorDTO.class);
        Creator entity = creatorMapper.toEntity(creatorDTO);

        when(creatorRepository.findById(creatorDTO.getId())).thenReturn(Optional.of(entity));
        // when
        CreatorDTO result = underTest.getOne(creatorDTO.getId());
        // then
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getLogin(), result.getLogin());
        assertEquals(entity.getFirstname(), result.getFirstname());
        assertEquals(entity.getLastname(), result.getLastname());
        assertEquals(entity.getPassword(), result.getPassword());
    }

    @DisplayName("Get one creator with no creator found")
    @Test
    void getOne_throwException_whenCreatorNotFound() {
        // given
        Long id = 1L;

        when(creatorRepository.findById(id)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.getOne(id));
        // then
        assertEquals(ErrorMessage.CREATOR_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @DisplayName("Delete one creator successfully")
    @Test
    void deleteOne_deleteCreator() {
        // given
        Long id = 1L;
        // when
        underTest.deleteOne(id);
        // then
        verify(creatorRepository).deleteById(id);
    }

    @DisplayName("Update one creator with same login")
    @Test
    void updateOne_throwException_whenLoginExists() {
        // given
        CreatorDTO creatorDTO = Instancio.create(CreatorDTO.class);
        creatorDTO.setId(1L);

        Creator entity = creatorMapper.toEntity(creatorDTO);
        entity.setId(2L);

        when(creatorRepository.findById(creatorDTO.getId())).thenReturn(Optional.of(entity));
        when(creatorRepository.findCreatorByLogin(creatorDTO.getLogin())).thenReturn(Optional.of(entity));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(creatorDTO));
        // then
        assertEquals(ErrorMessage.CREATOR_LOGIN_ALREADY_EXISTS.getText(), exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

        verify(creatorRepository, never()).save(any());
    }

    @DisplayName("Update one creator when creator not found")
    @Test
    void updateOne_throwException_whenCreatorNotFound() {
        // given
        CreatorDTO creatorDTO = Instancio.create(CreatorDTO.class);

        when(creatorRepository.findById(creatorDTO.getId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(creatorDTO));
        // then
        assertEquals(ErrorMessage.CREATOR_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

        verify(creatorRepository, never()).save(any());
    }

    @DisplayName("Update one creator successfully")
    @Test
    void updateOne_returnCreatorDTO_whenCreatorExists() {
        // given
        CreatorDTO creatorDTO = Instancio.create(CreatorDTO.class);

        Creator entity = creatorMapper.toEntity(creatorDTO);
        entity.setLogin("old login");
        entity.setLastname("old lastname");
        entity.setFirstname("old firstname");

        when(creatorRepository.findById(creatorDTO.getId())).thenReturn(Optional.of(entity));
        when(creatorRepository.findCreatorByLogin(creatorDTO.getLogin())).thenReturn(Optional.empty());
        when(creatorRepository.save(any())).thenReturn(entity);
        // when
        CreatorDTO result = underTest.updateOne(creatorDTO);
        // then
        assertNotNull(result);
        assertEquals(creatorDTO.getId(), result.getId());
        assertEquals(creatorDTO.getLogin(), result.getLogin());
        assertEquals(creatorDTO.getFirstname(), result.getFirstname());
        assertEquals(creatorDTO.getLastname(), result.getLastname());
        assertEquals(creatorDTO.getPassword(), result.getPassword());

        verify(creatorRepository).save(any());
    }

    @DisplayName("Get all creators successfully")
    @Test
    void getAll_returnListOfCreators() {
        // given
        List<Creator> creators = Instancio.ofList(Creator.class).size(2).create();
        List<CreatorDTO> expected = creators.stream().map(creatorMapper::toDTO).toList();

        when(creatorRepository.findAll()).thenReturn(creators);
        // when
        List<CreatorDTO> result = underTest.getAll();
        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }
}