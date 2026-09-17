package com.aac.ieojwo.board.domain;
import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="card_usage_logs",indexes=@Index(name="idx_card_usage_user_time",columnList="aac_user_id,occurred_at"))
public class CardUsageLog extends BaseTimeEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aac_user_id",nullable=false) private AacUser user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="card_id",nullable=false) private AacCard card;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private UsageAction action;
 @Column(nullable=false) private Instant occurredAt;
 protected CardUsageLog(){} public static CardUsageLog create(AacUser u,AacCard c,UsageAction a,Instant at){CardUsageLog l=new CardUsageLog();l.user=u;l.card=c;l.action=a;l.occurredAt=at;return l;}
 public Long getId(){return id;}public AacUser getUser(){return user;}public AacCard getCard(){return card;}public UsageAction getAction(){return action;}public Instant getOccurredAt(){return occurredAt;}
}
