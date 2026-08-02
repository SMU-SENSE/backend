package com.aac.ieojwo.aac.repository;

import com.aac.ieojwo.aac.domain.UserFavoriteSymbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteSymbolRepository extends JpaRepository<UserFavoriteSymbol, Long> {
    boolean existsByUserIdAndSymbolId(Long userId, Long symbolId);
    Optional<UserFavoriteSymbol> findByUserIdAndSymbolId(Long userId, Long symbolId);
    List<UserFavoriteSymbol> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
