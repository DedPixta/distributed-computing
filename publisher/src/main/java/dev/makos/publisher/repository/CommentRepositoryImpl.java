package dev.makos.publisher.repository;

import dev.makos.publisher.mapper.CommentMapper;
import dev.makos.publisher.model.dto.CommentCassandraDTO;
import dev.makos.publisher.model.entity.Comment;
import dev.makos.publisher.model.entity.Tweet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private static final String COUNTRY = "KZ";

    private final CommentMapper commentMapper;
    private final TweetRepository tweetRepository;

    @Value("${discussion.url}")
    private String url;

    @Override
    public Comment save(Comment comment) {
        CommentCassandraDTO body = commentMapper.toCassandraDTO(comment);
        body.setCountry(COUNTRY);



        try {
            RestClient restClient = RestClient.create();
            RestClient.RequestBodyUriSpec method = comment.getId() == null ? restClient.post(): restClient.put();
            CommentCassandraDTO dto = method
                    .uri(url + "/api/v1.0/comments")
                    .contentType(APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(CommentCassandraDTO.class);

            Comment entity = commentMapper.toEntity(dto);

            if (dto != null) {
                entity.setTweet(comment.getTweet());
            }

            return entity;
        } catch (RestClientResponseException e) {
            return comment;
        }
    }

    @Override
    public Optional<Comment> findById(Long id) {
        try {
            CommentCassandraDTO dto = RestClient.create().get()
                    .uri(url + "/api/v1.0/comments/{id}", id)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .body(CommentCassandraDTO.class);

            Comment entity = commentMapper.toEntity(dto);

            if (dto != null && dto.getTweetId() != null) {
                Optional<Tweet> tweet = tweetRepository.findById(dto.getTweetId());
                tweet.ifPresent(entity::setTweet);
            }

            return Optional.of(entity);
        } catch (RestClientResponseException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public void deleteById(Long id) {
        RestClient.create().delete()
                .uri(url + "/api/v1.0/comments/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<Comment> findAll() {
        try {
            List<CommentCassandraDTO> commentDTOs = RestClient.create().get()
                    .uri(url + "/api/v1.0/comments")
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (commentDTOs == null) {
                return List.of();
            }

            List<Comment> comments = commentDTOs.stream()
                    .map(commentMapper::toEntity)
                    .toList();

            for (Comment comment : comments) {
                if (comment.getTweet() != null && comment.getTweet().getId() != null) {
                    Optional<Tweet> tweet = tweetRepository.findById(comment.getTweet().getId());
                    tweet.ifPresent(comment::setTweet);
                }
            }

            return comments;
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }
}
