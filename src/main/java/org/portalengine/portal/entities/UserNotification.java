package org.portalengine.portal.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.portalengine.portal.Auditable;

import lombok.Data;

@Data
@Entity
@Table(name = UserNotification.TABLE_NAME)
public class UserNotification extends Auditable<String> {

    public static final String TABLE_NAME= "portal_user_notification";
    	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

    private String notification;
    private String icon;
    private String iconColor;
    private String badge;
    private String badgeColor;

    @ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id", columnDefinition = "bigserial" )
    @Type(type = "bigserial")
	private User user; 
}