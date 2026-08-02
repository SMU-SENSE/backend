package com.aac.ieojwo.aac.repository;

import com.aac.ieojwo.aac.domain.SymbolUsageLog;
import com.aac.ieojwo.aac.domain.UsageAction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SymbolUsageLogRepository extends JpaRepository<SymbolUsageLog, Long> {
    List<SymbolUsageLog> findByUserIdAndActionOrderByOccurredAtDesc(
            Long userId,
            UsageAction action,
            Pageable pageable
    );
}
