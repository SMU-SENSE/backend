package com.aac.ieojwo.symbol.repository;

import com.aac.ieojwo.symbol.domain.SymbolCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SymbolCategoryRepository extends JpaRepository<SymbolCategory, Long> {
    boolean existsByCode(String code);
    List<SymbolCategory> findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
}
