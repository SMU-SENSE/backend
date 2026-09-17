package com.aac.ieojwo.board.service;

import com.aac.ieojwo.board.domain.*;
import com.aac.ieojwo.board.dto.BoardDtos.*;
import com.aac.ieojwo.board.repository.*;
import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.common.exception.*;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.symbol.domain.SymbolCategory;
import com.aac.ieojwo.symbol.repository.*;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.repository.AacUserRepository;
import com.aac.ieojwo.user.service.AacUserAccessService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional(readOnly=true)
public class BoardService {
 private final BoardCategoryRepository categories; private final AacCardRepository cards; private final SymbolCategoryRepository catalogCategories; private final SymbolRepository catalogSymbols; private final AacUserRepository users; private final AacUserAccessService access; private final LiveEventService live;
 private final CardUsageLogRepository usageLogs;
 public BoardService(BoardCategoryRepository categories,AacCardRepository cards,SymbolCategoryRepository catalogCategories,SymbolRepository catalogSymbols,AacUserRepository users,AacUserAccessService access,LiveEventService live,CardUsageLogRepository usageLogs){this.categories=categories;this.cards=cards;this.catalogCategories=catalogCategories;this.catalogSymbols=catalogSymbols;this.users=users;this.access=access;this.live=live;this.usageLogs=usageLogs;}
 @Transactional public BoardResponse get(OidcUser p,Long userId){return get(access.requireAccessibleUser(p,userId));}
 @Transactional public BoardResponse getForDevice(AacUser user){return get(user);}
 private BoardResponse get(AacUser user){initialize(user);return response(user);}
 private void initialize(AacUser user){if(categories.existsByUserIdAndActiveTrue(user.getId()))return;Map<Long,BoardCategory> map=new HashMap<>();for(SymbolCategory source:catalogCategories.findAllByActiveTrueOrderByDisplayOrderAscIdAsc()){BoardCategory c=categories.save(BoardCategory.create(user,source.getName(),null,source.getDisplayOrder()));map.put(source.getId(),c);}catalogSymbols.findAllByActiveTrueOrderByDisplayOrderAscIdAsc().forEach(s->{BoardCategory c=map.get(s.getCategory().getId());if(c!=null)cards.save(AacCard.create(user,c,s.getName(),s.getImageUrl(),s.getTtsText(),s.isEmergency(),s.getDisplayOrder()));});user.incrementBoardVersion();live.publish(user.getId(),"BOARD_UPDATED",Map.of("version",user.getBoardVersion()));}
 private BoardResponse response(AacUser user){return new BoardResponse(user.getId(),user.getBoardVersion(),user.getGridSize(),user.getStatus(),categories.findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(user.getId()).stream().map(CategoryResponse::from).toList(),cards.findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(user.getId()).stream().map(CardResponse::from).toList());}
 @Transactional public CategoryResponse createCategory(OidcUser p,Long id,CreateCategoryRequest r){AacUser u=access.requireAccessibleUser(p,id);BoardCategory c=categories.save(BoardCategory.create(u,r.name(),r.color(),r.displayOrder()));changed(u);return CategoryResponse.from(c);}
 @Transactional public CategoryResponse updateCategory(OidcUser p,Long id,Long categoryId,UpdateCategoryRequest r){AacUser u=access.requireAccessibleUser(p,id);BoardCategory c=category(u,categoryId);c.update(r.name(),r.color(),r.displayOrder());changed(u);return CategoryResponse.from(c);}
 @Transactional public void deleteCategory(OidcUser p,Long id,Long categoryId){AacUser u=access.requireAccessibleUser(p,id);BoardCategory c=category(u,categoryId);if(!cards.findAllByCategoryIdAndActiveTrue(categoryId).isEmpty())throw new ConflictException("카테고리의 카드를 먼저 삭제하거나 이동해주세요.");c.deactivate();changed(u);}
 @Transactional public CardResponse createCard(OidcUser p,Long id,CreateCardRequest r){AacUser u=access.requireAccessibleUser(p,id);AacCard c=cards.save(AacCard.create(u,category(u,r.categoryId()),r.text(),r.imageUrl(),r.ttsText(),r.emergency(),r.displayOrder()));changed(u);return CardResponse.from(c);}
 @Transactional public CardResponse updateCard(OidcUser p,Long id,Long cardId,UpdateCardRequest r){AacUser u=access.requireAccessibleUser(p,id);AacCard c=card(u,cardId);c.update(r.categoryId()==null?null:category(u,r.categoryId()),r.text(),r.imageUrl(),r.ttsText(),r.emergency(),r.displayOrder());changed(u);return CardResponse.from(c);}
 @Transactional public CardResponse favorite(OidcUser p,Long id,Long cardId,FavoriteRequest r){AacUser u=access.requireAccessibleUser(p,id);AacCard c=card(u,cardId);c.setFavorite(r.favorite());changed(u);return CardResponse.from(c);}
 @Transactional public void deleteCard(OidcUser p,Long id,Long cardId){AacUser u=access.requireAccessibleUser(p,id);card(u,cardId).deactivate();changed(u);}
 @Transactional public Long recordUsage(AacUser u,Long cardId,UsageAction action,java.time.Instant occurredAt){AacCard c=card(u,cardId);CardUsageLog log=usageLogs.save(CardUsageLog.create(u,c,action,occurredAt==null?java.time.Instant.now():occurredAt));live.publish(u.getId(),"CARD_USED",Map.of("cardId",cardId,"action",action.name(),"occurredAt",log.getOccurredAt().toString()));return log.getId();}
 private BoardCategory category(AacUser u,Long id){BoardCategory c=categories.findById(id).orElseThrow(()->new ResourceNotFoundException("카테고리를 찾을 수 없습니다."));if(!c.getUser().getId().equals(u.getId())||!c.isActive())throw new ForbiddenException("다른 사용자의 카테고리입니다.");return c;}
 private AacCard card(AacUser u,Long id){AacCard c=cards.findById(id).orElseThrow(()->new ResourceNotFoundException("카드를 찾을 수 없습니다."));if(!c.getUser().getId().equals(u.getId())||!c.isActive())throw new ForbiddenException("다른 사용자의 카드입니다.");return c;}
 private void changed(AacUser u){u.incrementBoardVersion();live.publish(u.getId(),"BOARD_UPDATED",Map.of("version",u.getBoardVersion()));}
}
