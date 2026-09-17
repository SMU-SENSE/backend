package com.aac.ieojwo.board.dto;

import com.aac.ieojwo.board.domain.*;
import com.aac.ieojwo.user.domain.*;
import jakarta.validation.constraints.*;
import java.util.List;

public final class BoardDtos {
 private BoardDtos(){}
 public record CategoryResponse(Long id,String name,String color,int displayOrder){public static CategoryResponse from(BoardCategory c){return new CategoryResponse(c.getId(),c.getName(),c.getColor(),c.getDisplayOrder());}}
 public record CardResponse(Long id,Long categoryId,String text,String imageUrl,String ttsText,boolean emergency,boolean favorite,int displayOrder){public static CardResponse from(AacCard c){return new CardResponse(c.getId(),c.getCategory().getId(),c.getText(),c.getImageUrl(),c.getTtsText(),c.isEmergency(),c.isFavorite(),c.getDisplayOrder());}}
 public record BoardResponse(Long aacUserId,long version,GridSize gridSize,UserStatus status,List<CategoryResponse> categories,List<CardResponse> cards){}
 public record CreateCategoryRequest(@NotBlank @Size(max=50) String name,@Size(max=20) String color,@NotNull @Min(0) Integer displayOrder){}
 public record UpdateCategoryRequest(@Size(max=50) String name,@Size(max=20) String color,@Min(0) Integer displayOrder){}
 public record CreateCardRequest(@NotNull Long categoryId,@NotBlank @Size(max=80) String text,@Size(max=500) String imageUrl,@Size(max=200) String ttsText,boolean emergency,@Min(0) int displayOrder){}
 public record UpdateCardRequest(Long categoryId,@Size(max=80) String text,@Size(max=500) String imageUrl,@Size(max=200) String ttsText,Boolean emergency,@Min(0) Integer displayOrder){}
 public record FavoriteRequest(boolean favorite){}
}
