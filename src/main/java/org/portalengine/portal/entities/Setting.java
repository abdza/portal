package org.portalengine.portal.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = Setting.TABLE_NAME)
public class Setting extends Auditable<String> {
	
	public static final String TABLE_NAME= "portal_setting";

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String module;
	private String name;
	private String type;
	
	@Column(name="text")
	private String textValue;
	
	@DateTimeFormat(pattern="dd/MM/yyyy")
	@Column(name="date_value")
	private Date dateValue;
	
	@Column(name="number")
	private Long numberValue;
	
	@JsonIgnore
	public String getValue() {
		if(this.type.toLowerCase().equals("date")) {
			if(dateValue==null){
				return null;
			}
			return dateValue.toString();
		}
		else if(this.type.toLowerCase().equals("number")) {
			if(numberValue==null){
				return null;
			}
			return numberValue.toString();
		}
		return textValue;
	}
	
}
