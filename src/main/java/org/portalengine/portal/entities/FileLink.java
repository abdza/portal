package org.portalengine.portal.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.portalengine.portal.Auditable;

import lombok.Data;

@Data
@Entity
@Table(name = FileLink.TABLE_NAME)
public class FileLink extends Auditable<String> {
	
	public static final String TABLE_NAME= "portal_file";

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String module;
	
	@NotNull
	private String slug;
	
	private String name;
	
	private String path;
	
	@Column(name="filegroup")
	private String fileGroup;
	
	@Column(name="allowedroles")
	private String allowedRoles;
	
	@Column(name="sortnum")
	private Long sortNum;
	
	private String fileType;
}
