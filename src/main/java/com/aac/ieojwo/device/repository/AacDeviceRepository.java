package com.aac.ieojwo.device.repository;
import com.aac.ieojwo.device.domain.AacDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AacDeviceRepository extends JpaRepository<AacDevice,Long>{Optional<AacDevice> findByDeviceId(String deviceId);List<AacDevice> findAllByUserIdOrderByIdAsc(Long userId);}
