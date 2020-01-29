package org.portalengine.portal.User.Message;

import java.util.Date;

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
@Table(name = "portal_user_message")
public class UserMessage {
    	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

    private String title;
    private String message;
    private Date sentDate;

    @ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id" )
	private User user; 
    
    @ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "from_id" )
	private User from; 
}