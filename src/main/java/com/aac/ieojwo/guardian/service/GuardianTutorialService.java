package com.aac.ieojwo.guardian.service;
import com.aac.ieojwo.guardian.domain.Guardian;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
@Service @Transactional(readOnly=true)
public class GuardianTutorialService{
 public static final int CURRENT_VERSION=1;private final GuardianAccessService access;
 public GuardianTutorialService(GuardianAccessService access){this.access=access;}
 public TutorialResponse current(OidcUser p){return response(access.requireCurrentGuardian(p));}
 @Transactional public TutorialResponse complete(OidcUser p){Guardian g=access.requireCurrentGuardian(p);g.completeTutorial(CURRENT_VERSION,Instant.now());return response(g);}
 private TutorialResponse response(Guardian g){return new TutorialResponse(CURRENT_VERSION,g.getTutorialVersion()>=CURRENT_VERSION,g.getTutorialCompletedAt());}
 public record TutorialResponse(int currentVersion,boolean completed,Instant completedAt){}
}
