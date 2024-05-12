package org.example.distributedcomputing.repository;

import org.example.distributedcomputing.model.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    boolean existsByTitle(String title);

    Optional<Tweet> findTweetByTitle(String title);

}
