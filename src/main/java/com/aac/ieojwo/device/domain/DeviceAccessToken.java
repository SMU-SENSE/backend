package com.aac.ieojwo.device.domain;
import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="device_access_tokens",uniqueConstraints=@UniqueConstraint(name="uk_device_token_hash",columnNames="token_hash"))
public class DeviceAccessToken extends BaseTimeEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="device_id",nullable=false) private AacDevice device;
 @Column(name="token_hash",nullable=false,length=64) private String tokenHash;
 @Column(nullable=false) private Instant expiresAt; private Instant revokedAt;
 protected DeviceAccessToken(){}
 public static DeviceAccessToken create(AacDevice d,String hash,Instant expires){DeviceAccessToken t=new DeviceAccessToken();t.device=d;t.tokenHash=hash;t.expiresAt=expires;return t;}
 public boolean usableAt(Instant now){return revokedAt==null&&expiresAt.isAfter(now)&&device.getStatus()==DeviceStatus.ACTIVE;} public void revoke(Instant now){revokedAt=now;}
 public AacDevice getDevice(){return device;} public Instant getExpiresAt(){return expiresAt;}
}
