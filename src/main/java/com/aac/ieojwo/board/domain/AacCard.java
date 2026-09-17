package com.aac.ieojwo.board.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

@Entity @Table(name="aac_cards", indexes=@Index(name="idx_aac_card_user",columnList="aac_user_id,active,display_order"))
public class AacCard extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aac_user_id",nullable=false) private AacUser user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id",nullable=false) private BoardCategory category;
 @Column(nullable=false,length=80) private String text;
 @Column(length=500) private String imageUrl;
 @Column(nullable=false,length=200) private String ttsText;
 @Column(nullable=false) private boolean emergency;
 @Column(nullable=false) private boolean favorite;
 @Column(nullable=false) private int displayOrder;
 @Column(nullable=false) private boolean active;
 protected AacCard(){}
 public static AacCard create(AacUser user,BoardCategory category,String text,String imageUrl,String ttsText,boolean emergency,int order){AacCard c=new AacCard();c.user=user;c.category=category;c.text=text.trim();c.imageUrl=blank(imageUrl);c.ttsText=ttsText==null||ttsText.isBlank()?c.text:ttsText.trim();c.emergency=emergency;c.displayOrder=order;c.active=true;return c;}
 public void update(BoardCategory category,String text,String imageUrl,String ttsText,Boolean emergency,Integer order){if(category!=null)this.category=category;if(text!=null&&!text.isBlank())this.text=text.trim();if(imageUrl!=null)this.imageUrl=blank(imageUrl);if(ttsText!=null&&!ttsText.isBlank())this.ttsText=ttsText.trim();if(emergency!=null)this.emergency=emergency;if(order!=null)this.displayOrder=order;}
 public void setFavorite(boolean favorite){this.favorite=favorite;} public void deactivate(){active=false;favorite=false;}
 private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
 public Long getId(){return id;} public AacUser getUser(){return user;} public BoardCategory getCategory(){return category;} public String getText(){return text;} public String getImageUrl(){return imageUrl;} public String getTtsText(){return ttsText;} public boolean isEmergency(){return emergency;} public boolean isFavorite(){return favorite;} public int getDisplayOrder(){return displayOrder;} public boolean isActive(){return active;}
}
