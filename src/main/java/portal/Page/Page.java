package portal.Page;

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
@Table(name = "portal_page")
public class Page {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	@Size(min=3, message="Module must be at least 3 characters long")
	private String module;
	
	@NotNull
	@Size(min=3, message="Slug must be at least 3 characters long")
	private String slug;
	
	@NotNull
	@Size(min=3, message="Title must be at least 3 characters long")
	private String title;
	
	@NotNull
	@Size(min=5, message="Content must be at least 5 characters long")
	@org.hibernate.annotations.Type( type = "text" )
	private String content;	

}
