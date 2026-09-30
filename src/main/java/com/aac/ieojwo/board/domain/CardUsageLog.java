package com.aac.ieojwo.board.domain;
import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.device.domain.AacDevice;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="card_usage_logs",indexes=@Index(name="idx_card_usage_user_time",columnList="aac_user_id,occurred_at"))
public class CardUsageLog extends BaseTimeEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aac_user_id",nullable=false) private AacUser user;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="card_id") private AacCard card;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="device_id") private AacDevice device;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private UsageAction action;
 @Column(length=80) private String displayTextSnapshot;
 @Column(length=500) private String spokenText;
 @Column(nullable=false) private Instant occurredAt;
 protected CardUsageLog(){} public static CardUsageLog create(AacDevice device,AacCard card,UsageAction action,String spokenText,Instant at){CardUsageLog log=new CardUsageLog();log.user=device.getUser();log.device=device;log.card=card;log.action=action;log.displayTextSnapshot=card==null?null:card.getText();log.spokenText=blank(spokenText);log.occurredAt=at;return log;}
 private static String blank(String value){return value==null||value.isBlank()?null:value.trim();}
 public Long getId(){return id;}public AacUser getUser(){return user;}public AacCard getCard(){return card;}public AacDevice getDevice(){return device;}public UsageAction getAction(){return action;}public String getDisplayTextSnapshot(){return displayTextSnapshot;}public String getSpokenText(){return spokenText;}public Instant getOccurredAt(){return occurredAt;}
}
