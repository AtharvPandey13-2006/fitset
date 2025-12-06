package com.fitset.repository;

import com.fitset.model.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRepository extends MongoRepository<Trainer, String> {
    List<Trainer> findByAvailableTrue();
    List<Trainer> findByCertifiedTrueAndAvailableTrue();
}
