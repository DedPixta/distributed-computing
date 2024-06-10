package dev.makos.publisher.repository;

import dev.makos.publisher.mapper.CommentMapper;
import dev.makos.publisher.model.dto.CommentCassandraDTO;
import dev.makos.publisher.model.entity.Comment;
import dev.makos.publisher.model.entity.Tweet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentMapper commentMapper;
    private final TweetRepository tweetRepository;

    @Value("${discussion.url}")
    private String url;

    @Override
    public Comment save(Comment comment) {
        return null;
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
        return false;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public List<Comment> findAll() {
        return List.of();
    }
}
