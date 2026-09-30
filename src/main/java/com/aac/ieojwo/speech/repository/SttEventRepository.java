package com.aac.ieojwo.speech.repository;

import com.aac.ieojwo.speech.domain.SttEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface SttEventRepository extends JpaRepository<SttEvent, Long> {
    long deleteByRecordedAtBefore(Instant cutoff);
}
