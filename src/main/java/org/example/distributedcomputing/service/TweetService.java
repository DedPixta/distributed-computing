package org.example.distributedcomputing.service;

import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.exception.CustomException;
import org.example.distributedcomputing.mapper.TweetMapper;
import org.example.distributedcomputing.model.dto.TweetDTO;
import org.example.distributedcomputing.model.entity.Creator;
import org.example.distributedcomputing.model.entity.Tweet;
import org.example.distributedcomputing.repository.CreatorRepository;
import org.example.distributedcomputing.repository.TweetRepository;
import org.example.distributedcomputing.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TweetService {

    private final TweetMapper tweetMapper;
    private final TweetRepository tweetRepository;
    private final CreatorRepository creatorRepository;

    @Transactional
    public TweetDTO saveOne(TweetDTO tweetDTO) {
        Tweet entity = tweetMapper.toEntity(tweetDTO);
        Creator creator = creatorRepository.findById(tweetDTO.getCreatorId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.CREATOR_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        if (tweetRepository.existsByTitle(tweetDTO.getTitle())) {
            throw CustomException.builder()
                    .message(ErrorMessage.TWEET_TITLE_ALREADY_EXISTS.getText())
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .build();
        }

        entity.setCreator(creator);
        entity = tweetRepository.save(entity);
        return tweetMapper.toDTO(entity);
    }

    public TweetDTO getOne(Long id) {
        return tweetRepository.findById(id)
                .map(tweetMapper::toDTO)
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.TWEET_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    @Transactional
    public void deleteOne(Long id) {
        if (!tweetRepository.existsById(id)) {
            throw CustomException.builder()
                    .message(ErrorMessage.TWEET_NOT_FOUND.getText())
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        tweetRepository.deleteById(id);
    }

    @Transactional
    public TweetDTO updateOne(TweetDTO tweetDTO) {
        Tweet entity = tweetRepository.findById(tweetDTO.getId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.TWEET_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        if (titleExists(tweetDTO)) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message(ErrorMessage.TWEET_TITLE_ALREADY_EXISTS.getText())
                    .build();
        }

        Long creatorId = entity.getCreator().getId();
        Long requestCreatorId = tweetDTO.getCreatorId();
        if (!creatorId.equals(requestCreatorId)) {
            Creator creator = creatorRepository.findById(tweetDTO.getCreatorId())
                    .orElseThrow(() -> CustomException.builder()
                            .message(ErrorMessage.CREATOR_NOT_FOUND.getText())
                            .httpStatus(HttpStatus.NOT_FOUND)
                            .build());

            entity.setCreator(creator);
        }

        entity.setContent(tweetDTO.getContent());
        entity.setTitle(tweetDTO.getTitle());
        entity = tweetRepository.save(entity);
        return tweetMapper.toDTO(entity);
    }

    private boolean titleExists(TweetDTO tweetDTO) {
        return tweetRepository.findTweetByTitle(tweetDTO.getTitle())
                .filter(tweet -> !tweet.getId().equals(tweetDTO.getId()))
                .isPresent();
    }

    public List<TweetDTO> getAll() {
        return tweetRepository.findAll().stream()
                .map(tweetMapper::toDTO)
                .toList();
    }
}
