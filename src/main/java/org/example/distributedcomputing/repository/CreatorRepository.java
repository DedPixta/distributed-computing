package org.example.distributedcomputing.repository;

import org.example.distributedcomputing.model.entity.Creator;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreatorRepository extends CrudRepository<Creator, Long> {

    List<Creator> findAll();

}
