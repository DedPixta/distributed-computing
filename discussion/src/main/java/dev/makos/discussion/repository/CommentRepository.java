package dev.makos.discussion.repository;

import dev.makos.discussion.model.entity.Comment;
import dev.makos.discussion.model.entity.CommentKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends CassandraRepository<Comment, CommentKey>{

    @Query("SELECT * FROM tbl_comment WHERE id = :id ALLOW FILTERING")
    Optional<Comment> findCommentById(Long id);

}
