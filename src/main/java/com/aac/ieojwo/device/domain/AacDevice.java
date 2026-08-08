package com.aac.ieojwo.device.domain;
import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="aac_devices", uniqueConstraints=@UniqueConstraint(name="uk_aac_device_device_id",columnNames="device_id"), indexes=@Index(name="idx_aac_devices_user",columnList="aac_user_id"))
public class AacDevice extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aac_user_id",nullable=false) private AacUser user;
 @Column(name="device_id",nullable=false,length=100) private String deviceId;
 @Column(length=100) private String deviceName;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private DeviceType deviceType;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private DeviceStatus status;
 @Column(nullable=false) private Instant pairedAt;
 private Instant lastSeenAt;
 protected AacDevice(){}
 public static AacDevice create(AacUser u,String id,String name,DeviceType type,Instant now){AacDevice d=new AacDevice();d.reconnect(u,name,type,now);d.deviceId=id;return d;}
 public void reconnect(AacUser u,String name,DeviceType type,Instant now){user=u;deviceName=name==null||name.isBlank()?null:name.trim();deviceType=type;status=DeviceStatus.ACTIVE;pairedAt=now;lastSeenAt=now;}
 public Long getId(){return id;} public AacUser getUser(){return user;} public String getDeviceId(){return deviceId;} public String getDeviceName(){return deviceName;} public DeviceType getDeviceType(){return deviceType;} public DeviceStatus getStatus(){return status;} public Instant getPairedAt(){return pairedAt;} public Instant getLastSeenAt(){return lastSeenAt;}
}
