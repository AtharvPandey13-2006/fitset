package com.fitset.repository;

import com.fitset.model.DailyTracking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTrackingRepository extends MongoRepository<DailyTracking, String> {
    Optional<DailyTracking> findByUserIdAndDate(String userId, LocalDate date);
    List<DailyTracking> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<DailyTracking> findByUserIdOrderByDateDesc(String userId);
}
