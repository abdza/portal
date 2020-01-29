package org.portalengine.portal.User.Notification;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.portalengine.portal.User.User;

import lombok.Data;

@Entity
@Data
@Table(name = "portal_user_notification")
public class UserNotification {
    	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

    private String notification;
    private String icon;
    private String iconColor;
    private String badge;
    private String badgeColor;

    @ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id" )
	private User user; 
}