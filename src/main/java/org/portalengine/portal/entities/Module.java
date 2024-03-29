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
@Table(name = Module.TABLE_NAME)
public class Module extends Auditable<String> {

	public static final String TABLE_NAME= "portal_module";
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String name;	

}
