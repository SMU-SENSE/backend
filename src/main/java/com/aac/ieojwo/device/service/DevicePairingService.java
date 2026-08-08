package com.aac.ieojwo.device.service;

import com.aac.ieojwo.common.exception.*;
import com.aac.ieojwo.device.domain.*;
import com.aac.ieojwo.device.dto.*;
import com.aac.ieojwo.device.repository.*;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.service.GuardianAccessService;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.repository.AacUserRepository;
import com.aac.ieojwo.user.service.AacUserAccessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.Base64;

@Service @Transactional(readOnly=true)
public class DevicePairingService {
 private static final Duration TTL=Duration.ofMinutes(10); private final SecureRandom random=new SecureRandom();
 private final DevicePairingSessionRepository sessions; private final AacDeviceRepository devices; private final AacUserRepository users;
 private final AacUserAccessService access; private final GuardianAccessService guardianAccess; private final PairingHashService hashes;
 private final String qrPrefix;
 public DevicePairingService(DevicePairingSessionRepository sessions,AacDeviceRepository devices,AacUserRepository users,AacUserAccessService access,GuardianAccessService guardianAccess,PairingHashService hashes,@Value("${app.pairing.qr-prefix:malmoa://pair?token=}")String qrPrefix){this.sessions=sessions;this.devices=devices;this.users=users;this.access=access;this.guardianAccess=guardianAccess;this.hashes=hashes;this.qrPrefix=qrPrefix;}
 @Transactional public PairingResponse issue(OidcUser principal,Long userId){
  access.requireAccessibleUser(principal,userId); AacUser user=users.findByIdForUpdate(userId).orElseThrow(); Guardian guardian=guardianAccess.requireCurrentGuardian(principal); Instant now=Instant.now();
  sessions.findAllByUserIdAndStatus(userId,PairingStatus.ACTIVE).forEach(s->s.revoke(now));
  String token=newToken(); String code=newCode();
  DevicePairingSession session=sessions.save(DevicePairingSession.create(user,guardian,hashes.hash(token),hashes.hash(code),now.plus(TTL)));
  return new PairingResponse(session.getId(),qrPrefix+token,code,session.getExpiresAt(),TTL.toSeconds());
 }
 @Transactional(noRollbackFor=GoneException.class) public CurrentPairingResponse current(OidcUser principal,Long userId){
  access.requireAccessibleUser(principal,userId); DevicePairingSession s=sessions.findFirstByUserIdAndStatusOrderByIdDesc(userId,PairingStatus.ACTIVE).orElseThrow(()->new ResourceNotFoundException("활성 기기 연결 세션이 없습니다.")); Instant now=Instant.now();
  if(s.expireIfNeeded(now))throw new GoneException("기기 연결 세션이 만료되었습니다.");
  return new CurrentPairingResponse(s.getId(),s.getStatus(),s.getExpiresAt(),Math.max(0,Duration.between(now,s.getExpiresAt()).toSeconds()));
 }
 @Transactional(noRollbackFor=GoneException.class) public DeviceClaimResponse claimQr(QrClaimRequest r){return claim(sessions.findByQrHashForUpdate(hashes.hash(r.token())).orElseThrow(()->new ResourceNotFoundException("QR 연결 토큰을 찾을 수 없습니다.")),r.deviceId(),r.deviceName(),r.deviceType());}
 @Transactional(noRollbackFor=GoneException.class) public DeviceClaimResponse claimCode(CodeClaimRequest r){return claim(sessions.findByCodeHashForUpdate(hashes.hash(r.code())).orElseThrow(()->new ResourceNotFoundException("초대 코드를 찾을 수 없습니다.")),r.deviceId(),r.deviceName(),r.deviceType());}
 private DeviceClaimResponse claim(DevicePairingSession s,String deviceId,String name,DeviceType type){Instant now=Instant.now();if(s.expireIfNeeded(now))throw new GoneException("기기 연결 세션이 만료되었습니다.");if(s.getStatus()==PairingStatus.USED)throw new ConflictException("이미 사용된 기기 연결 세션입니다.");if(s.getStatus()==PairingStatus.REVOKED)throw new ConflictException("취소된 기기 연결 세션입니다.");if(s.getStatus()!=PairingStatus.ACTIVE)throw new ConflictException("사용할 수 없는 기기 연결 세션입니다.");AacDevice d=devices.findByDeviceId(deviceId).map(existing->{existing.reconnect(s.getUser(),name,type,now);return existing;}).orElseGet(()->devices.save(AacDevice.create(s.getUser(),deviceId,name,type,now)));s.markUsed(now);return new DeviceClaimResponse(s.getUser().getId(),d.getDeviceId(),d.getPairedAt());}
 public java.util.List<DeviceResponse> devices(OidcUser p,Long id){access.requireAccessibleUser(p,id);return devices.findAllByUserIdOrderByIdAsc(id).stream().map(DeviceResponse::from).toList();}
 private String newToken(){byte[] b=new byte[32];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
 private String newCode(){for(int i=0;i<20;i++){String c="%06d".formatted(random.nextInt(1_000_000));if(!sessions.existsByInviteCodeHash(hashes.hash(c)))return c;}throw new IllegalStateException("초대 코드를 생성할 수 없습니다.");}
}
