package com.aac.ieojwo.location.repository;

import com.aac.ieojwo.location.domain.LocationSample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LocationSampleRepository extends JpaRepository<LocationSample, Long> {
    Optional<LocationSample> findFirstByUserIdOrderByRecordedAtDesc(Long id);
    List<LocationSample> findAllByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(Long id, Instant from, Instant to);
    long deleteByRecordedAtBefore(Instant cutoff);
}
