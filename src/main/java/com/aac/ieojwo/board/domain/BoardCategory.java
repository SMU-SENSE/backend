package com.aac.ieojwo.board.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

@Entity @Table(name="board_categories", indexes=@Index(name="idx_board_category_user",columnList="aac_user_id,active,display_order"))
public class BoardCategory extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aac_user_id",nullable=false) private AacUser user;
 @Column(nullable=false,length=50) private String name;
 @Column(length=20) private String color;
 @Column(nullable=false) private int displayOrder;
 @Column(nullable=false) private boolean active;
 protected BoardCategory(){}
 public static BoardCategory create(AacUser user,String name,String color,int order){BoardCategory c=new BoardCategory();c.user=user;c.name=name.trim();c.color=blank(color);c.displayOrder=order;c.active=true;return c;}
 public void update(String name,String color,Integer order){if(name!=null&&!name.isBlank())this.name=name.trim();if(color!=null)this.color=blank(color);if(order!=null)this.displayOrder=order;}
 public void deactivate(){active=false;}
 private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
 public Long getId(){return id;} public AacUser getUser(){return user;} public String getName(){return name;} public String getColor(){return color;} public int getDisplayOrder(){return displayOrder;} public boolean isActive(){return active;}
}
