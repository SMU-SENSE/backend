package com.aac.ieojwo.device.domain;
import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="device_pairing_sessions", indexes={@Index(name="idx_pairing_user_status",columnList="aac_user_id,status"),@Index(name="idx_pairing_expires",columnList="expires_at")})
public class DevicePairingSession extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aac_user_id",nullable=false) private AacUser user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="guardian_id",nullable=false) private Guardian guardian;
 @Column(nullable=false,length=64,unique=true) private String qrTokenHash;
 @Column(nullable=false,length=64,unique=true) private String inviteCodeHash;
 @Column(nullable=false) private Instant expiresAt;
 private Instant usedAt; private Instant revokedAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PairingStatus status;
 protected DevicePairingSession(){}
 public static DevicePairingSession create(AacUser u,Guardian g,String tokenHash,String codeHash,Instant expires){DevicePairingSession s=new DevicePairingSession();s.user=u;s.guardian=g;s.qrTokenHash=tokenHash;s.inviteCodeHash=codeHash;s.expiresAt=expires;s.status=PairingStatus.ACTIVE;return s;}
 public boolean expireIfNeeded(Instant now){if(status==PairingStatus.ACTIVE&&!expiresAt.isAfter(now)){status=PairingStatus.EXPIRED;return true;}return status==PairingStatus.EXPIRED;}
 public void markUsed(Instant now){status=PairingStatus.USED;usedAt=now;}
 public void revoke(Instant now){if(status==PairingStatus.ACTIVE){status=PairingStatus.REVOKED;revokedAt=now;}}
 public Long getId(){return id;} public AacUser getUser(){return user;} public Instant getExpiresAt(){return expiresAt;} public Instant getUsedAt(){return usedAt;} public Instant getRevokedAt(){return revokedAt;} public PairingStatus getStatus(){return status;}
}
