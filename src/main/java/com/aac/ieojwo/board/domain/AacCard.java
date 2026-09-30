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
 @Column(length=200) private String ttsText;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private ImageSourceType imageSourceType;
 @Column(length=100) private String imageSourceName;
 @Column(length=100) private String imageLicense;
 @Column(length=500) private String imageAttributionUrl;
 @Column(nullable=false) private boolean emergency;
 @Column(nullable=false) private boolean favorite;
 @Column(nullable=false) private boolean important;
 @Column(nullable=false) private boolean systemCard;
 @Column(nullable=false) private int displayOrder;
 @Column(nullable=false) private boolean active;
 protected AacCard(){}
 public static AacCard create(AacUser user,BoardCategory category,String text,String imageUrl,String ttsText,boolean emergency,int order){return create(user,category,text,imageUrl,ttsText,emergency,order,ImageSourceType.SYSTEM_DEFAULT,null,null,null,false,false);}
 public static AacCard createSystem(AacUser user,BoardCategory category,String text,String imageUrl,String ttsText,boolean emergency,int order){return create(user,category,text,imageUrl,ttsText,emergency,order,ImageSourceType.SYSTEM_DEFAULT,null,null,null,false,true);}
 public static AacCard create(AacUser user,BoardCategory category,String text,String imageUrl,String ttsText,boolean emergency,int order,ImageSourceType sourceType,String sourceName,String license,String attributionUrl,boolean important,boolean systemCard){AacCard c=new AacCard();c.user=user;c.category=category;c.text=text.trim();c.imageUrl=blank(imageUrl);c.ttsText=blank(ttsText);c.imageSourceType=sourceType==null?ImageSourceType.SYSTEM_DEFAULT:sourceType;c.imageSourceName=blank(sourceName);c.imageLicense=blank(license);c.imageAttributionUrl=blank(attributionUrl);c.emergency=emergency;c.important=important;c.systemCard=systemCard;c.displayOrder=order;c.active=true;return c;}
 public void update(BoardCategory category,String text,String ttsText,Boolean emergency,Integer order,Boolean important){if(category!=null)this.category=category;if(text!=null&&!text.isBlank())this.text=text.trim();if(ttsText!=null)this.ttsText=blank(ttsText);if(emergency!=null)this.emergency=emergency;if(order!=null)this.displayOrder=order;if(important!=null)this.important=important;}
 public void updateImage(String imageUrl,ImageSourceType sourceType,String sourceName,String license,String attributionUrl){this.imageUrl=blank(imageUrl);this.imageSourceType=sourceType==null?ImageSourceType.SYSTEM_DEFAULT:sourceType;this.imageSourceName=blank(sourceName);this.imageLicense=blank(license);this.imageAttributionUrl=blank(attributionUrl);}
 public void setFavorite(boolean favorite){this.favorite=favorite;} public void deactivate(){active=false;favorite=false;}
 private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
 public Long getId(){return id;} public AacUser getUser(){return user;} public BoardCategory getCategory(){return category;} public String getText(){return text;} public String getImageUrl(){return imageUrl;} public String getTtsText(){return ttsText==null?text:ttsText;} public String getCustomTtsText(){return ttsText;} public ImageSourceType getImageSourceType(){return imageSourceType;} public String getImageSourceName(){return imageSourceName;} public String getImageLicense(){return imageLicense;} public String getImageAttributionUrl(){return imageAttributionUrl;} public boolean isEmergency(){return emergency;} public boolean isFavorite(){return favorite;} public boolean isImportant(){return important;} public boolean isSystemCard(){return systemCard;} public int getDisplayOrder(){return displayOrder;} public boolean isActive(){return active;}
}
