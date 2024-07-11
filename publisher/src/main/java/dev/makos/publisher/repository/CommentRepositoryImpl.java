package dev.makos.publisher.repository;

import dev.makos.publisher.kafka.producer.KafkaCommentProducer;
import dev.makos.publisher.mapper.CommentMapper;
import dev.makos.publisher.model.State;
import dev.makos.publisher.model.dto.CommentCassandraDTO;
import dev.makos.publisher.model.entity.Comment;
import dev.makos.publisher.model.entity.Tweet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
    private final KafkaCommentProducer kafkaProducer;

    @Value("${discussion.url}")
    private String url;

    @Override
    public Comment save(Comment comment) {
        comment.setState(State.PENDING);

        CommentCassandraDTO commentDTO = commentMapper.toCassandraDTO(comment);
        commentDTO.setCountry(COUNTRY);
        commentDTO.setMethod(HttpMethod.POST.toString());

        kafkaProducer.sendAndReceive(commentDTO);

        return comment;
    }

    @Override
    public Optional<Comment> findById(Long id) {

        CommentCassandraDTO commentDTO = new CommentCassandraDTO();
        commentDTO.setId(id);
        commentDTO.setCountry(COUNTRY);
        commentDTO.setMethod(HttpMethod.GET.toString());

        try {
            CommentCassandraDTO commentCassandraDTO = kafkaProducer.sendAndReceive(commentDTO);
            return Optional.of(commentMapper.toEntity(commentCassandraDTO));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public void deleteById(Long id) {

        CommentCassandraDTO commentDTO = new CommentCassandraDTO();
        commentDTO.setId(id);
        commentDTO.setCountry(COUNTRY);
        commentDTO.setMethod(HttpMethod.DELETE.toString());

        kafkaProducer.sendAndReceive(commentDTO);
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
