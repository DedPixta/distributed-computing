package dev.makos.publisher.repository;

import dev.makos.publisher.model.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    boolean existsByTitle(String title);

    Optional<Tweet> findTweetByTitle(String title);

}
