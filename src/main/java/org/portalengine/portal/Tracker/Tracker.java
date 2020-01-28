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
import javax.validation.constraints.Size;

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
public class Tracker {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String module;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String title;
	
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
		System.out.println("This is going to delete the " + this.title + " tracker");
	}
	

	
	public void updateDb(JdbcTemplate jdbctemplate) {
		if(this.dataTable.length()>0) {
			System.out.println("Checking existance of table:" + this.dataTable);
			// Check whether data table already exists
			SqlRowSet trythis = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = 'dbo' "
					+ "and TABLE_NAME = '" + this.dataTable + "'");
			if(!trythis.next()) {
				// Data table does not exists yet, so please create
				System.out.println("Creating table:" + this.dataTable);
				jdbctemplate.execute("create table " + this.dataTable + " (id INT NOT NULL IDENTITY(1,1),"
						+ "CONSTRAINT PK_" + this.dataTable + " PRIMARY KEY(id))");
			}
			trythis = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = 'dbo' "
					+ "and TABLE_NAME = '" + this.dataTable + "' and COLUMN_NAME = 'record_status'");
			if(!trythis.next()) {
				// Check to see if column record_status doesn't exist yet
				if(!this.trackerType.equals("Statement")) {
					// Please add record_status if type is a tracker (ie not a statement)
					jdbctemplate.execute("alter table " + this.dataTable + " add [record_status] varchar(256) NULL");
				}
			}
			trythis = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = 'dbo' "
					+ "and TABLE_NAME = '" + this.dataTable + "' and COLUMN_NAME = 'dataupdate_id'");
			if(!trythis.next()) {
				System.out.println("Creating field: dataupdate_id");
				jdbctemplate.execute("alter table " + this.dataTable + " add [dataupdate_id] numeric(24,0) NULL");
			}
			System.out.println("Type is:" + this.trackerType + "-----------------");
			if(this.trackerType.equals("Trailed Tracker")) {
				System.out.println("It's a trailed tracker");
				// Need to check whether need to create updates table
				if(this.updatesTable.length()>0) {
					System.out.println("Updates table name exists");
					SqlRowSet trytrails = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.TABLES where "
							+ "TABLE_SCHEMA = 'dbo' and TABLE_NAME = '" + this.updatesTable + "'");
					if(!trytrails.next()) {
						System.out.println("updates table not in db");
						// If updates table does not exists please create one
						jdbctemplate.execute("create table " + this.updatesTable + " (id INT NOT NULL IDENTITY(1,1), "
								+ "[attachment_id] numeric(19,0), [description] text, [record_id] numeric(19,0),"
								+ "[update_date] datetime, [updater_id] numeric(19,0), [status]varchar(255),"
								+ "[changes]text,[allowedroles]varchar(255),CONSTRAINT PK_" + this.updatesTable + " PRIMARY KEY(id))");
					}
					}
			}
			for(TrackerField field: this.fields) {
				field.updateDb(jdbctemplate);
			}
		}
	}
}
