package dev.makos.discussion.repository;

import dev.makos.discussion.model.entity.IdCounter;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdRepository extends CrudRepository<IdCounter, String> {

    @Query("UPDATE ids SET next_id = next_id + 1 WHERE name = :name")
    void increment(String name);

    @Query("SELECT next_id FROM ids WHERE name = :name")
    Long getCurrentId(String name);

}
