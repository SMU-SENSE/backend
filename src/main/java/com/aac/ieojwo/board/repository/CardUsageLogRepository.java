package com.aac.ieojwo.board.repository;
import com.aac.ieojwo.board.domain.CardUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;import java.util.*;
import org.springframework.data.domain.Pageable;
public interface CardUsageLogRepository extends JpaRepository<CardUsageLog,Long>{List<CardUsageLog> findAllByUserIdAndOccurredAtBetweenOrderByOccurredAtAsc(Long userId,Instant from,Instant to);List<CardUsageLog> findAllByUserIdAndCardIsNotNullOrderByOccurredAtDesc(Long userId,Pageable pageable);long deleteByOccurredAtBefore(Instant cutoff);}
