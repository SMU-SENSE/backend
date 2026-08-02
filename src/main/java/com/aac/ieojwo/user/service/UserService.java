package com.aac.ieojwo.user.service;

import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.dto.CreateUserRequest;
import com.aac.ieojwo.user.dto.UpdateUserSettingsRequest;
import com.aac.ieojwo.user.dto.UserResponse;
import com.aac.ieojwo.user.repository.AacUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AacUserRepository userRepository;

    public UserService(AacUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        AacUser user = AacUser.create(request.name().trim(), request.mode(), request.gridSize());
        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse updateSettings(Long userId, UpdateUserSettingsRequest request) {
        AacUser user = getUser(userId);
        user.updateSettings(request.mode(), request.gridSize());
        return UserResponse.from(user);
    }

    public AacUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));
    }
}
