package org.portalengine.portal.entities;

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

import org.portalengine.portal.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tracker_role")
public class TrackerRole extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@JsonIgnore
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker;
	
	@NotNull
	private String name;

	@org.hibernate.annotations.Type( type = "text" )
	private String roleRule;
	
	private String roleType;
	
	public TrackerRole copy(Tracker destTracker) {
		TrackerRole newrole = new TrackerRole();
		newrole.tracker = destTracker;
		newrole.name = this.name;
		newrole.roleRule = this.roleRule;
		newrole.roleType = this.roleType;
		return newrole;
	}
	
}
