package org.portalengine.portal.Tracker;

import java.util.ArrayList;
import java.util.List;

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
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Role.TrackerRole;
import org.portalengine.portal.Tracker.Status.TrackerStatus;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

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
	private String formFields;
	private String displayFields;
	private String initialStatus;
	
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
	
	public void add(TrackerField field) {
		fields.add(field);
		field.setTracker(this);
	}
	
	public String fieldsList() {
		String toret = "";
		for(TrackerField field:this.fields) {
			toret += "," + field.getName();
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
	
	@PrePersist
	@PreUpdate
	void checkTables() {
		if(this.dataTable.length()==0) {
			this.dataTable = "trak_" + this.slug + "_data";
		}
		if(this.trackerType.equals("Trailed Tracker") && this.updatesTable.length()==0) {
			this.updatesTable = "trak_" + this.slug + "_updates";
		}
	}
	
	@PreRemove
	void beforeDelete() {
		System.out.println("This is going to delete the " + this.name + " tracker");
	}
	

	
	public void updateDb(JdbcTemplate jdbctemplate) {
		if(this.dataTable.length()>0) {
			System.out.println("Checking existance of table:" + this.dataTable);
			// Check whether data table already exists
			String toquery = "select count(*) as result from INFORMATION_SCHEMA.TABLES where "
					+ " TABLE_NAME = '" + this.dataTable.toUpperCase() + "'";
			SqlRowSet trythis = jdbctemplate.queryForRowSet(toquery);
			trythis.next();
			if(trythis.getInt("result")==0) {
				// Data table does not exists yet, so please create
				jdbctemplate.execute("create table " + this.dataTable.toUpperCase() + " (ID INT NOT NULL IDENTITY(1,1),"
						+ "CONSTRAINT PK_" + this.dataTable.toUpperCase() + " PRIMARY KEY(ID))");
			}
			trythis = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.COLUMNS where "
					+ " TABLE_NAME = '" + this.dataTable.toUpperCase() + "' and COLUMN_NAME = 'RECORD_STATUS'");
			trythis.next();
			if(trythis.getInt("result")==0) {
				// Check to see if column record_status doesn't exist yet
				if(!this.trackerType.equals("Statement")) {
					// Please add record_status if type is a tracker (ie not a statement)
					jdbctemplate.execute("alter table " + this.dataTable.toUpperCase() + " add RECORD_STATUS varchar(256) NULL");
				}
			}
			trythis = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.COLUMNS where "
					+ " TABLE_NAME = '" + this.dataTable.toUpperCase() + "' and COLUMN_NAME = 'DATAUPDATE_ID'");
			trythis.next();
			if(trythis.getInt("result")==0) {
				jdbctemplate.execute("alter table " + this.dataTable.toUpperCase() + " add DATAUPDATE_ID numeric(24,0) NULL");
			}
			System.out.println("Type is:" + this.trackerType + "-----------------");
			if(this.trackerType.equals("Trailed Tracker")) {
				// Need to check whether need to create updates table
				if(this.updatesTable.length()>0) {
					SqlRowSet trytrails = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.TABLES where "
							+ " TABLE_NAME = '" + this.updatesTable.toUpperCase() + "'");
					trytrails.next();
					if(trytrails.getInt("result")==0) {
						// If updates table does not exists please create one
						jdbctemplate.execute("create table " + this.updatesTable.toUpperCase() + " (ID INT NOT NULL IDENTITY(1,1), "
								+ "ATTACHMENT_ID numeric(19,0), DESCRIPTION text, RECORD_ID numeric(19,0),"
								+ "UPDATE_DATE datetime, UPDATER_ID numeric(19,0), STATUS varchar(255),"
								+ "CHANGES text, ALLOWEDROLES varchar(255),CONSTRAINT PK_" + this.updatesTable.toUpperCase() + " PRIMARY KEY(ID))");
					}
					}
			}
			for(TrackerField field: this.fields) {
				field.updateDb(jdbctemplate);
			}
		}
	}
}
