package org.portalengine.portal.User.Task;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.User.User;

import lombok.Data;

@Entity
@Data
@Table(name = "portal_user_task")
public class UserTask extends Auditable<String> {
    	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

    private String title;
    private String task;
    private String category;
    private String module;
    private String slug;
    private Date sentDate;

    @ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id" )
	private User user; 
 
}