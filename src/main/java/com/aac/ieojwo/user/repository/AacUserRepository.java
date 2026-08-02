package com.aac.ieojwo.user.repository;

import com.aac.ieojwo.user.domain.AacUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AacUserRepository extends JpaRepository<AacUser, Long> {
}
