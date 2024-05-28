package dev.makos.publisher.repository;

import dev.makos.publisher.model.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    List<Comment> findAll();

}
