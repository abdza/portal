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
@Table(name = "portal_tracker_transition")
public class TrackerTransition extends Auditable<String> {
	
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
	
	public TrackerTransition copy(Tracker destTracker) {
		TrackerTransition newtransition = new TrackerTransition();
		newtransition.tracker = destTracker;
		newtransition.name = this.name;
		newtransition.editFields = this.editFields;
		newtransition.displayFields = this.displayFields;
		newtransition.requiredFields = this.requiredFields;
		newtransition.enabledCondition = this.enabledCondition;
		newtransition.updateTrails = this.updateTrails;
		newtransition.submitButtonText = this.submitButtonText;
		newtransition.gotoPrevStatusList = this.gotoPrevStatusList;
		newtransition.nextStatus = this.nextStatus;
		newtransition.prevStatus = this.prevStatus;
		newtransition.allowedRoles = this.allowedRoles;
		return newtransition;
	}
}
