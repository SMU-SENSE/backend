package com.aac.ieojwo.symbol.repository;

import com.aac.ieojwo.symbol.domain.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    List<Symbol> findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
    List<Symbol> findAllByCategoryIdAndActiveTrueOrderByDisplayOrderAscIdAsc(Long categoryId);
    List<Symbol> findAllByEmergencyTrueAndActiveTrueOrderByDisplayOrderAscIdAsc();
}
