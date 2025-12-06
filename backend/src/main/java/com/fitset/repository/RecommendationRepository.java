package com.fitset.repository;

import com.fitset.model.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    Optional<Recommendation> findTopByUserIdOrderByGeneratedAtDesc(String userId);
}
