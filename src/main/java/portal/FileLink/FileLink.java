package portal.FileLink;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_file")
public class FileLink {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	@Size(min=3, message="Module must be at least 3 characters long")
	private String module;
	
	@NotNull
	@Size(min=3, message="Slug must be at least 3 characters long")
	private String slug;
	
	private String name;
	
	private String path;
	
	private String fileGroup;
	
	private String allowedRoles;
	
	private Long sortNum;
	
	private String type;
}
