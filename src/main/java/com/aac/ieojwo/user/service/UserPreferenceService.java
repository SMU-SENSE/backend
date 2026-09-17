package com.aac.ieojwo.user.service;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.user.domain.*;
import com.aac.ieojwo.user.dto.UserResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
@Service @Transactional(readOnly=true)
public class UserPreferenceService{
 private final AacUserAccessService access;private final LiveEventService live;
 public UserPreferenceService(AacUserAccessService access,LiveEventService live){this.access=access;this.live=live;}
 @Transactional public UserResponse sentenceLevel(OidcUser p,Long id,int level){AacUser u=access.requireAccessibleUser(p,id);u.updateSentenceLevel(level);live.publish(id,"SETTINGS_UPDATED",Map.of("sentenceLevel",level));return UserResponse.from(u);}
 @Transactional public UserResponse status(OidcUser p,Long id,UserStatus status){AacUser u=access.requireAccessibleUser(p,id);u.updateStatus(status);live.publish(id,"STATUS_UPDATED",Map.of("status",status.name()));return UserResponse.from(u);}
}
