package org.portalengine.portal.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.portalengine.portal.Auditable;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_page")
public class PortalPage extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String module;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String title;
	
	private String page_type;
	
	private Boolean runable;
	
	private Boolean requireLogin;
	
	private String allowedRoles;
	
	@NotNull
	@org.hibernate.annotations.Type( type = "text" )
	private String content;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String pageData;
	
	private Boolean published;
	
	public String contentPreview() {
		if(this.content.length()>50) {
			return this.content.substring(0,50);
		}
		else {
			return this.content;
		}
	}

}
