package com.aac.ieojwo.device.repository;
import com.aac.ieojwo.device.domain.DeviceAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.*;
public interface DeviceAccessTokenRepository extends JpaRepository<DeviceAccessToken,Long>{@EntityGraph(attributePaths={"device","device.user"}) Optional<DeviceAccessToken> findByTokenHash(String hash);List<DeviceAccessToken> findAllByDeviceId(Long deviceId);}
