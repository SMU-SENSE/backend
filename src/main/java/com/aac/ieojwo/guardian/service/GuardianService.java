package com.aac.ieojwo.guardian.service;

import com.aac.ieojwo.common.exception.ConflictException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.domain.UserGuardian;
import com.aac.ieojwo.guardian.dto.CreateGuardianRequest;
import com.aac.ieojwo.guardian.dto.GuardianResponse;
import com.aac.ieojwo.guardian.dto.LinkGuardianRequest;
import com.aac.ieojwo.guardian.repository.GuardianRepository;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final UserGuardianRepository userGuardianRepository;
    private final UserService userService;

    public GuardianService(
            GuardianRepository guardianRepository,
            UserGuardianRepository userGuardianRepository,
            UserService userService
    ) {
        this.guardianRepository = guardianRepository;
        this.userGuardianRepository = userGuardianRepository;
        this.userService = userService;
    }

    @Transactional
    public GuardianResponse create(CreateGuardianRequest request) {
        String email = request.email().trim().toLowerCase();
        if (guardianRepository.existsByEmail(email)) {
            throw new ConflictException("이미 등록된 보호자 이메일입니다.");
        }
        Guardian guardian = Guardian.create(request.name().trim(), email, normalize(request.phone()));
        return GuardianResponse.from(guardianRepository.save(guardian));
    }

    public List<GuardianResponse> findAll() {
        return guardianRepository.findAll().stream().map(GuardianResponse::from).toList();
    }

    @Transactional
    public GuardianResponse linkToUser(Long userId, LinkGuardianRequest request) {
        AacUser user = userService.getUser(userId);
        Guardian guardian = guardianRepository.findById(request.guardianId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "보호자를 찾을 수 없습니다. guardianId=" + request.guardianId()
                ));

        if (userGuardianRepository.existsByUserIdAndGuardianId(userId, request.guardianId())) {
            throw new ConflictException("이미 사용자와 연결된 보호자입니다.");
        }

        UserGuardian relation = UserGuardian.create(
                user, guardian, request.role(), request.primaryGuardian()
        );
        return GuardianResponse.from(userGuardianRepository.save(relation));
    }

    public List<GuardianResponse> findByUser(Long userId) {
        userService.getUser(userId);
        return userGuardianRepository.findAllByUserIdOrderByPrimaryGuardianDescIdAsc(userId)
                .stream()
                .map(GuardianResponse::from)
                .toList();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
