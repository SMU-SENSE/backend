package com.aac.ieojwo.aac.repository;

import com.aac.ieojwo.aac.domain.UtteranceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UtteranceLogRepository extends JpaRepository<UtteranceLog, Long> {
    List<UtteranceLog> findAllByUserIdAndOccurredAtAfterOrderByOccurredAtAsc(Long userId, LocalDateTime since);
}
