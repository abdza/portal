package portal.Tracker.Transition;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import portal.Tracker.Tracker;

@Data
@Entity
@Table(name = "portal_tracker_transition")
public class TrackerTransition {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker;
	
	@NotNull
	private String name;

	@org.hibernate.annotations.Type( type = "text" )
	private String editFields;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String displayFields;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String requiredFields;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String enabledCondition;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String updateTrails;
	
	private String submitButtonText;
	
	private boolean gotoPrevStatusList;
	
	private String nextStatus;
	private String prevStatus;
	private String allowedRoles;
}
