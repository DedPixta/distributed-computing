package org.example.distributedcomputing.service;

import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.exception.CustomException;
import org.example.distributedcomputing.mapper.CommentMapper;
import org.example.distributedcomputing.model.dto.CommentDTO;
import org.example.distributedcomputing.model.entity.Comment;
import org.example.distributedcomputing.model.entity.Tweet;
import org.example.distributedcomputing.repository.CommentRepository;
import org.example.distributedcomputing.repository.TweetRepository;
import org.example.distributedcomputing.util.ErrorMessage;
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
