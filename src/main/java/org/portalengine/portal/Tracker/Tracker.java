package org.portalengine.portal.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.portalengine.portal.Auditable;
import org.portalengine.portal.Page.PortalPage;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Role.TrackerRole;
import org.portalengine.portal.Tracker.Status.TrackerStatus;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.Role.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tracker")
public class Tracker extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String module;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String name;
	
	private String dataTable;
	private String updatesTable;
	private String trackerType;
	private String listFields;
	private String excelFields;
	private String formFields;
	private String displayFields;
	private String searchFields;
	private String filterFields;
	private String initialStatus;
	private Long nodeId;
	private String viewListRoles;
	private String addRoles;
	private String detailRoles;
	private String deleteRoles;
	private String editRoles;
	private String defaultStatus;
	private String postCreate;
	private String postEdit;
	private String postDelete;
	private String postDataUpdate;
	
	@OneToMany(
			mappedBy = "tracker",
			orphanRemoval = true) 
	private List<TrackerField> fields = new ArrayList<>();
	
	@OneToMany(
			mappedBy = "tracker",
			orphanRemoval = true)
	private List<TrackerRole> roles = new ArrayList<>();
	
	@OneToMany(
			mappedBy = "tracker",
			orphanRemoval = true)
	private List<TrackerStatus> statuses = new ArrayList<>();
	
	@OneToMany(
			mappedBy = "tracker",
			orphanRemoval = true)
	private List<TrackerTransition> transitions = new ArrayList<>();
	
	@Transient
	public String[] typeOptions = {"Statement","Tracker","Trailed Tracker"};
	
	public Tracker copy(Long newNodeId) {
		Tracker newtracker = new Tracker();
		newtracker.module = this.module;
		newtracker.slug = this.slug;
		newtracker.name = this.name;
		newtracker.trackerType = this.trackerType;
		newtracker.listFields = this.listFields;
		newtracker.formFields = this.formFields;
		newtracker.excelFields = this.excelFields;
		newtracker.searchFields = this.searchFields;
		newtracker.filterFields = this.filterFields;
		newtracker.displayFields = this.displayFields;
		newtracker.initialStatus = this.initialStatus;
		
		newtracker.updatesTable = this.updatesTable;		
		newtracker.viewListRoles = this.viewListRoles;
		newtracker.addRoles = this.addRoles;
		newtracker.detailRoles = this.detailRoles;
		newtracker.deleteRoles = this.deleteRoles;
		newtracker.editRoles = this.editRoles;
		newtracker.defaultStatus = this.defaultStatus;
		newtracker.postCreate = this.postCreate;
		newtracker.postEdit = this.postEdit;
		newtracker.postDelete = this.postDelete;
		newtracker.postDataUpdate = this.postDataUpdate;
		
		newtracker.dataTable = this.dataTable + "_" + newNodeId.toString();
		if(this.updatesTable.length()>0) {
			newtracker.updatesTable = this.updatesTable + "_" + newNodeId.toString();
		}
		newtracker.nodeId = newNodeId;
		return newtracker;
	}
	
	public void add(TrackerField field) {
		fields.add(field);
		field.setTracker(this);
	}
	
	public String fieldsList() {
		String toret = "";
		boolean firstfield = true;
		for(TrackerField field:this.fields) {
			if(!firstfield) {
				toret += ",";
			}
			else {
				firstfield = false;
			}
			toret += field.getName();
		}
		return toret;
	}
	
	public void remove(TrackerField field) {
		fields.remove(field);
		field.setTracker(null);
	}
	
	public void add(TrackerRole role) {
		roles.add(role);
		role.setTracker(this);
	}
	
	public void remove(TrackerRole role) {
		fields.remove(role);
		role.setTracker(null);
	}
	
	public void add(TrackerStatus status) {
		statuses.add(status);
		status.setTracker(this);
	}
	
	public void remove(TrackerStatus status) {
		statuses.remove(status);
		status.setTracker(null);
	}
	
	public void add(TrackerTransition transition) {
		transitions.add(transition);
		transition.setTracker(this);
	}
	
	public void remove(TrackerTransition transition) {
		transitions.remove(transition);
		transition.setTracker(null);
	}
	
	public List<UserRole> module_roles(User user) {
		List<UserRole> roles = new ArrayList<UserRole>();
		for(TrackerRole tr:this.getRoles()) {
			if(tr.getRoleType().equals("User Role")) {
				for(UserRole ur:user.getRoles()) {
					if(ur.getModule().equals(this.getModule()) && ur.getRole().equals(tr.getName()) ) {
						roles.add(ur);
					}
				}
			}
		}
		return roles;
	}
	
	@PrePersist
	@PreUpdate
	void checkTables() {
		if(this.dataTable.length()==0) {
			this.dataTable = "trak_" + this.module + "_" + this.slug + "_data";
			this.dataTable = this.dataTable.toLowerCase();
		}
		if(this.trackerType.equals("Trailed Tracker") && this.updatesTable.length()==0) {
			this.updatesTable = "trak_" + this.module + "_" + this.slug + "_updates";
			this.updatesTable = this.updatesTable.toLowerCase();
		}
	}
	
	@PreRemove
	void beforeDelete() {
		System.out.println("This is going to delete the " + this.name + " tracker");
	}	
}
