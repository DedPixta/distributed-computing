package dev.makos.publisher.service;

import dev.makos.publisher.exception.CustomException;
import dev.makos.publisher.mapper.StickerMapper;
import dev.makos.publisher.mapper.StickerMapperImpl;
import dev.makos.publisher.model.dto.StickerDTO;
import dev.makos.publisher.model.entity.Sticker;
import dev.makos.publisher.repository.StickerRepository;
import dev.makos.publisher.util.ErrorMessage;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StickerServiceTest {

    @Spy
    private StickerMapper stickerMapper = new StickerMapperImpl();

    @Mock
    private StickerRepository stickerRepository;

    @InjectMocks
    private StickerService underTest;

    @DisplayName("Save one sticker successfully")
    @Test
    void saveOne_returnStickerDTO(){
        // given
        StickerDTO stickerDTO = Instancio.create(StickerDTO.class);

        Sticker sticker = stickerMapper.toEntity(stickerDTO);
        sticker.setId(1L);

        when(stickerRepository.save(any())).thenReturn(sticker);
        // when
        StickerDTO result = underTest.saveOne(stickerDTO);
        // then
        assertNotNull(result);
        assertEquals(sticker.getId(), result.getId());
        assertEquals(stickerDTO.getName(), result.getName());
    }

    @DisplayName("Get one sticker not found")
    @Test
    void getOne_throwException_whenStickerNotFound(){
        // given
        Long id = 1L;
        when(stickerRepository.findById(id)).thenReturn(java.util.Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.getOne(id));
        // then
        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @DisplayName("Get one sticker successfully")
    @Test
    void getOne_returnStickerDTO(){
        // given
        StickerDTO stickerDTO = Instancio.create(StickerDTO.class);

        Sticker sticker = stickerMapper.toEntity(stickerDTO);
        sticker.setId(1L);

        when(stickerRepository.findById(1L)).thenReturn(java.util.Optional.of(sticker));
        // when
        StickerDTO result = underTest.getOne(sticker.getId());
        // then
        assertNotNull(result);
        assertEquals(sticker.getId(), result.getId());
        assertEquals(stickerDTO.getName(), result.getName());
    }

    @DisplayName("Delete one sticker successfully")
    @Test
    void deleteOne(){
        // given
        Long id = 1L;
        when(stickerRepository.existsById(id)).thenReturn(true);
        // when
        underTest.deleteOne(id);
        // then
        verify(stickerRepository).deleteById(id);
    }

    @DisplayName("Delete one sticker not found")
    @Test
    void deleteOne_throwException_whenStickerNotFound(){
        // given
        Long id = 1L;
        when(stickerRepository.existsById(id)).thenReturn(false);
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deleteOne(id));
        // then
        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @DisplayName("Update one sticker not found")
    @Test
    void updateOne_throwException_whenStickerNotFound(){
        // given
        StickerDTO stickerDTO = Instancio.create(StickerDTO.class);
        when(stickerRepository.findById(stickerDTO.getId())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.updateOne(stickerDTO));
        // then
        assertEquals(ErrorMessage.STICKER_NOT_FOUND.getText(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @DisplayName("Update one sticker successfully")
    @Test
    void updateOne_returnStickerDTO(){
        // given
        StickerDTO stickerDTO = Instancio.create(StickerDTO.class);

        Sticker sticker = stickerMapper.toEntity(stickerDTO);
        sticker.setId(stickerDTO.getId());
        sticker.setName("old name");

        when(stickerRepository.findById(stickerDTO.getId())).thenReturn(Optional.of(sticker));
        when(stickerRepository.save(any())).thenReturn(sticker);
        // when
        StickerDTO result = underTest.updateOne(stickerDTO);
        // then
        assertNotNull(result);
        assertEquals(stickerDTO.getId(), result.getId());
        assertEquals(stickerDTO.getName(), result.getName());

        verify(stickerRepository).save(any());
    }

    @DisplayName("Get all stickers")
    @Test
    void getAll_returnListOfStickerDTO(){
        // given
        List<Sticker> stickers = Instancio.ofList(Sticker.class).size(2).create();
        List<StickerDTO> expected = stickers.stream().map(stickerMapper::toDTO).toList();

        when(stickerRepository.findAll()).thenReturn(stickers);
        // when
        var result = underTest.getAll();
        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }
}