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

@Data
@Entity
@Table(name = UserMessage.TABLE_NAME)
public class UserMessage extends Auditable<String> {

	public static final String TABLE_NAME= "portal_user_message";
    	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

    private String title;
    private String message;
    private Date sentDate;

    @ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "user_id", columnDefinition = "bigserial" )
    @Type(type = "bigserial")
	private User user; 
    
    @ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "from_id", columnDefinition = "bigserial"  )
	@Type(type = "bigserial")
	private User from; 
}