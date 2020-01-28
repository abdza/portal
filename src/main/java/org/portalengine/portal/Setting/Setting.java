package portal.Setting;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_setting")
public class Setting {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String module;
	private String name;
	private String textValue;
	
	@DateTimeFormat(pattern="dd/MM/yyyy")
	private Date dateValue;
	private Long number;
	
	@PrePersist
	@PreUpdate
	void checkValues() {
		
	}
}
