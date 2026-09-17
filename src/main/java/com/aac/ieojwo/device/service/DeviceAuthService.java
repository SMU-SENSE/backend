package com.aac.ieojwo.device.service;
import com.aac.ieojwo.common.exception.UnauthorizedException;
import com.aac.ieojwo.device.domain.*;
import com.aac.ieojwo.device.repository.DeviceAccessTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
@Service @Transactional(readOnly=true)
public class DeviceAuthService{
 public record IssuedToken(String raw,Instant expiresAt){}
 private static final Duration TTL=Duration.ofDays(90);private final SecureRandom random=new SecureRandom();private final DeviceAccessTokenRepository tokens;private final PairingHashService hashes;
 public DeviceAuthService(DeviceAccessTokenRepository tokens,PairingHashService hashes){this.tokens=tokens;this.hashes=hashes;}
 @Transactional public IssuedToken issue(AacDevice d){Instant now=Instant.now();tokens.findAllByDeviceId(d.getId()).forEach(t->t.revoke(now));byte[] b=new byte[32];random.nextBytes(b);String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(b);Instant expires=now.plus(TTL);tokens.save(DeviceAccessToken.create(d,hashes.hash(raw),expires));return new IssuedToken(raw,expires);}
 @Transactional public AacDevice authenticate(String authorization){if(authorization==null||!authorization.startsWith("Bearer "))throw new UnauthorizedException("기기 인증 토큰이 필요합니다.");String raw=authorization.substring(7).trim();DeviceAccessToken t=tokens.findByTokenHash(hashes.hash(raw)).orElseThrow(()->new UnauthorizedException("유효하지 않은 기기 토큰입니다."));if(!t.usableAt(Instant.now()))throw new UnauthorizedException("만료되거나 폐기된 기기 토큰입니다.");t.getDevice().heartbeat(Instant.now());return t.getDevice();}
 @Transactional public void revoke(AacDevice d){Instant now=Instant.now();tokens.findAllByDeviceId(d.getId()).forEach(t->t.revoke(now));d.revoke();}
}
