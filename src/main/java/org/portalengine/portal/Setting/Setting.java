package org.portalengine.portal.Setting;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_setting")
public class Setting extends Auditable<String> {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String module;
	private String name;
	private String type;
	
	private String textValue;
	
	@DateTimeFormat(pattern="dd/MM/yyyy")
	private Date dateValue;
	
	private Long numberValue;
	
	public String getValue() {
		if(this.type.equals("date")) {
			return dateValue.toString();
		}
		else if(this.type.equals("number")) {
			return numberValue.toString();
		}
		return textValue;
	}
	
}
