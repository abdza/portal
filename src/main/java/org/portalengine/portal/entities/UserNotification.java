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

@Entity
@Data
@Table(name = "portal_user_notification")
public class UserNotification extends Auditable<String> {
    	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    private String notification;
    private String icon;
    private String iconColor;
    private String badge;
    private String badgeColor;

    @ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id", columnDefinition = "numeric(19,0)" )
    @Type(type = "big_decimal")
	private User user; 
}