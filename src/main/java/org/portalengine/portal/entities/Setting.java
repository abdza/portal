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
@Table(name = "settings")
public class Setting extends Auditable<String> {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String module;
	private String name;
	private String type;
	
	@Column(name="text")
	private String textValue;
	
	@DateTimeFormat(pattern="dd/MM/yyyy")
	private Date dateValue;
	
	@Column(name="number")
	private Long numberValue;

	@org.springframework.data.annotation.Version
	protected long version;
	
	@JsonIgnore
	public String getValue() {
		if(this.type.equals("date")) {
			if(dateValue==null){
				return null;
			}
			return dateValue.toString();
		}
		else if(this.type.equals("number")) {
			if(numberValue==null){
				return null;
			}
			return numberValue.toString();
		}
		return textValue;
	}
	
}
