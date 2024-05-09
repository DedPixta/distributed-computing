package org.example.distributedcomputing.repository;

import org.example.distributedcomputing.model.entity.Sticker;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends CrudRepository<Sticker, Long> {

    List<Sticker> findAll();

}
