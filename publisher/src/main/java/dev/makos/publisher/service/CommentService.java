package dev.makos.publisher.service;

import dev.makos.publisher.exception.CustomException;
import dev.makos.publisher.mapper.CommentMapper;
import dev.makos.publisher.model.dto.CommentDTO;
import dev.makos.publisher.model.entity.Comment;
import dev.makos.publisher.model.entity.Tweet;
import dev.makos.publisher.repository.CommentRepository;
import dev.makos.publisher.repository.TweetRepository;
import dev.makos.publisher.util.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final TweetRepository tweetRepository;

    @Transactional
    public CommentDTO saveOne(CommentDTO commentDTO) {
        Comment entity = commentMapper.toEntity(commentDTO);
        Tweet tweet = tweetRepository.findById(commentDTO.getTweetId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.TWEET_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        entity.setTweet(tweet);
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }

    public CommentDTO getOne(Long id) {
        return commentRepository.findById(id)
                .map(commentMapper::toDTO)
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    @Transactional
    public void deleteOne(Long id) {
        if (!commentRepository.existsById(id)) {
            throw CustomException.builder()
                    .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        commentRepository.deleteById(id);
    }

    @Transactional
    public CommentDTO updateOne(CommentDTO commentDTO) {
        Comment entity = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> CustomException.builder()
                        .message(ErrorMessage.COMMENT_NOT_FOUND.getText())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());

        Long tweetId = entity.getTweet().getId();

        if (!tweetId.equals(commentDTO.getTweetId())) {
            Tweet tweet = tweetRepository.findById(commentDTO.getTweetId())
                    .orElseThrow(() -> CustomException.builder()
                            .message(ErrorMessage.TWEET_NOT_FOUND.getText())
                            .httpStatus(HttpStatus.NOT_FOUND)
                            .build());
            entity.setTweet(tweet);
        }

        entity.setContent(commentDTO.getContent());
        entity = commentRepository.save(entity);
        return commentMapper.toDTO(entity);
    }

    public List<CommentDTO> getAll() {
        return commentRepository.findAll().stream()
                .map(commentMapper::toDTO)
                .toList();
    }
}
