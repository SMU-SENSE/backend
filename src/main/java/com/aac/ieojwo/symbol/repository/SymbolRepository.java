package com.aac.ieojwo.symbol.repository;

import com.aac.ieojwo.symbol.domain.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    // 기본 카드 목록: 커스텀 카드(owner 있음)는 소유 사용자 API 에서만 보인다.
    List<Symbol> findAllByActiveTrueAndOwnerIsNullOrderByDisplayOrderAscIdAsc();
    List<Symbol> findAllByCategoryIdAndActiveTrueAndOwnerIsNullOrderByDisplayOrderAscIdAsc(Long categoryId);
    List<Symbol> findAllByEmergencyTrueAndActiveTrueAndOwnerIsNullOrderByDisplayOrderAscIdAsc();
    List<Symbol> findAllByOwnerIdAndActiveTrueOrderByIdAsc(Long ownerId);
    List<Symbol> findAllByCardCodeIn(Collection<String> cardCodes);
}
