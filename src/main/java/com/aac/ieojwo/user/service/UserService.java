package com.aac.ieojwo.user.service;

import com.aac.ieojwo.common.exception.BadRequestException;
import com.aac.ieojwo.guardian.domain.*;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.guardian.service.GuardianAccessService;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.dto.*;
import com.aac.ieojwo.user.repository.AacUserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional(readOnly = true)
public class UserService {
 private final AacUserRepository userRepository; private final UserGuardianRepository linkRepository;
 private final GuardianAccessService guardianAccessService; private final AacUserAccessService userAccessService;
 public UserService(AacUserRepository userRepository, UserGuardianRepository linkRepository,
  GuardianAccessService guardianAccessService, AacUserAccessService userAccessService) {
  this.userRepository=userRepository; this.linkRepository=linkRepository; this.guardianAccessService=guardianAccessService; this.userAccessService=userAccessService;
 }
 @Transactional public UserResponse create(OidcUser principal, CreateUserRequest request) {
  Guardian guardian=guardianAccessService.requireCurrentGuardian(principal);
  if (!request.isRelationshipDetailValid()) throw new BadRequestException("OTHER 관계에는 relationshipDetail이 필수입니다.");
  AacUser user=userRepository.save(AacUser.createProfile(request.name().trim(), request.birthDate(), request.emergencyContact(), request.notes(), request.profileImageUrl(), request.mode(), request.gridSize()));
  linkRepository.save(UserGuardian.create(user, guardian, GuardianRole.PRIMARY, true, request.relationshipType(), request.relationshipDetail()));
  return UserResponse.from(user);
 }
 public List<UserResponse> findAll(OidcUser principal) { Guardian g=guardianAccessService.requireCurrentGuardian(principal); return linkRepository.findAllByGuardianIdOrderByIdAsc(g.getId()).stream().map(UserGuardian::getUser).map(UserResponse::from).toList(); }
 public UserResponse findById(OidcUser principal, Long id) { return UserResponse.from(userAccessService.requireAccessibleUser(principal,id)); }
 @Transactional public UserResponse updateSettings(OidcUser p,Long id,UpdateUserSettingsRequest r){ AacUser u=userAccessService.requireAccessibleUser(p,id);u.updateSettings(r.mode(),r.gridSize());return UserResponse.from(u);}
 @Transactional public UserResponse updateGrid(OidcUser p,Long id,UpdateGridRequest r){AacUser u=userAccessService.requireAccessibleUser(p,id);u.updateGrid(r.gridSize());return UserResponse.from(u);}
 @Transactional public UserResponse updateVoice(OidcUser p,Long id,UpdateVoiceSettingsRequest r){AacUser u=userAccessService.requireAccessibleUser(p,id);u.updateVoiceSettings(r.voiceType(),r.speechRate());return UserResponse.from(u);}
 public OnboardingSummaryResponse summary(OidcUser p,Long id){ AacUser u=userAccessService.requireAccessibleUser(p,id); Guardian g=guardianAccessService.requireCurrentGuardian(p); UserGuardian link=linkRepository.findByUserIdAndGuardianId(id,g.getId()).orElseThrow(); return OnboardingSummaryResponse.from(u,link); }
 @Transactional public OnboardingSummaryResponse confirm(OidcUser p,Long id){AacUser u=userAccessService.requireAccessibleUser(p,id); if(u.getSetupStep().ordinal()<com.aac.ieojwo.user.domain.AacUserSetupStep.VOICE_COMPLETED.ordinal()) throw new BadRequestException("격자와 음성 설정을 먼저 완료해주세요.");u.confirmSetup();return summary(p,id);}
 public AacUser requireAccessibleUser(OidcUser p,Long id){return userAccessService.requireAccessibleUser(p,id);}
}
