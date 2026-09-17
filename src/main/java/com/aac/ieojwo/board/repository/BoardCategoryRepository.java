package com.aac.ieojwo.board.repository;
import com.aac.ieojwo.board.domain.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface BoardCategoryRepository extends JpaRepository<BoardCategory,Long>{List<BoardCategory> findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(Long userId);boolean existsByUserIdAndActiveTrue(Long userId);}
