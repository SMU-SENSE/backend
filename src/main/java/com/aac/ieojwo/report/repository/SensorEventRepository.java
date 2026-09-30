package com.aac.ieojwo.report.repository;

import com.aac.ieojwo.report.domain.SensorEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SensorEventRepository extends JpaRepository<SensorEvent, Long> {
    List<SensorEvent> findAllByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(Long id, Instant from, Instant to);
    long deleteByRecordedAtBefore(Instant cutoff);
}
