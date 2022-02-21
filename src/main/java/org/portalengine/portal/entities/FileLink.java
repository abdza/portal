package org.portalengine.portal.entities;

import javax.persistence.Column;
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
@Table(name = "file_link")
public class FileLink extends Auditable<String> {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
