package dev.makos.discussion.repository;

import dev.makos.discussion.model.entity.Comment;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends CassandraRepository<Comment, Long>{
}
