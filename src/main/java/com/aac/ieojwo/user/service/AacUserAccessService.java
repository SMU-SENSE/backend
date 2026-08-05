package com.aac.ieojwo.user.service;

import com.aac.ieojwo.common.exception.ForbiddenException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.guardian.service.GuardianAccessService;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.repository.AacUserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AacUserAccessService {
    private final AacUserRepository userRepository;
    private final UserGuardianRepository userGuardianRepository;
    private final GuardianAccessService guardianAccessService;

    public AacUserAccessService(AacUserRepository userRepository, UserGuardianRepository userGuardianRepository,
                                GuardianAccessService guardianAccessService) {
        this.userRepository = userRepository;
        this.userGuardianRepository = userGuardianRepository;
        this.guardianAccessService = guardianAccessService;
    }

    public AacUser requireAccessibleUser(OidcUser principal, Long userId) {
        AacUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));
        Guardian guardian = guardianAccessService.requireCurrentGuardian(principal);
        if (!userGuardianRepository.existsByUserIdAndGuardianId(userId, guardian.getId())) {
            throw new ForbiddenException("해당 AAC 사용자를 관리할 권한이 없습니다.");
        }
        return user;
    }
}
