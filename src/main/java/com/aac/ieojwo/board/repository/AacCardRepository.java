package com.aac.ieojwo.board.repository;
import com.aac.ieojwo.board.domain.AacCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AacCardRepository extends JpaRepository<AacCard,Long>{List<AacCard> findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(Long userId);List<AacCard> findAllByCategoryIdAndActiveTrue(Long categoryId);}
