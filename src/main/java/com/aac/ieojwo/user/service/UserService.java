package com.aac.ieojwo.user.service;

import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.domain.GuardianRole;
import com.aac.ieojwo.guardian.domain.UserGuardian;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.guardian.service.GuardianAccessService;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.dto.CreateUserRequest;
import com.aac.ieojwo.user.dto.UpdateUserSettingsRequest;
import com.aac.ieojwo.user.dto.UserResponse;
import com.aac.ieojwo.user.repository.AacUserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AacUserRepository userRepository;
    private final UserGuardianRepository userGuardianRepository;
    private final GuardianAccessService guardianAccessService;
    private final AacUserAccessService userAccessService;

    public UserService(AacUserRepository userRepository, UserGuardianRepository userGuardianRepository,
                       GuardianAccessService guardianAccessService, AacUserAccessService userAccessService) {
        this.userRepository = userRepository;
        this.userGuardianRepository = userGuardianRepository;
        this.guardianAccessService = guardianAccessService;
        this.userAccessService = userAccessService;
    }

    @Transactional
    public UserResponse create(OidcUser principal, CreateUserRequest request) {
        Guardian guardian = guardianAccessService.requireCurrentGuardian(principal);
        AacUser user = userRepository.save(AacUser.create(request.name().trim(), request.mode(), request.gridSize()));
        userGuardianRepository.save(UserGuardian.create(user, guardian, GuardianRole.PRIMARY, true));
        return UserResponse.from(user);
    }

    public List<UserResponse> findAll(OidcUser principal) {
        Guardian guardian = guardianAccessService.requireCurrentGuardian(principal);
        return userGuardianRepository.findAllByGuardianIdOrderByIdAsc(guardian.getId()).stream()
                .map(UserGuardian::getUser)
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(OidcUser principal, Long userId) {
        return UserResponse.from(userAccessService.requireAccessibleUser(principal, userId));
    }

    @Transactional
    public UserResponse updateSettings(OidcUser principal, Long userId, UpdateUserSettingsRequest request) {
        AacUser user = userAccessService.requireAccessibleUser(principal, userId);
        user.updateSettings(request.mode(), request.gridSize());
        return UserResponse.from(user);
    }

    public AacUser requireAccessibleUser(OidcUser principal, Long userId) {
        return userAccessService.requireAccessibleUser(principal, userId);
    }
}
