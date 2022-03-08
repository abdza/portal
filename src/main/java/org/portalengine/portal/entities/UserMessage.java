package org.portalengine.portal.entities;

import java.util.Date;

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
@Table(name = "portal_user_message")
public class UserMessage extends Auditable<String> {
    	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    private String title;
    private String message;
    private Date sentDate;

    @ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "user_id", columnDefinition = "numeric(19,0)" )
    @Type(type = "big_decimal")
	private User user; 
    
    @ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "from_id", columnDefinition = "numeric(19,0)"  )
	private User from; 
}