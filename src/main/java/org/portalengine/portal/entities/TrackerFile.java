package org.portalengine.portal.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

@Data
@Entity
@Table(name = TrackerFile.TABLE_NAME)
public class TrackerFile extends Auditable<String> {

	public static final String TABLE_NAME= "portal_tracker_file";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
		
	private String name;
	
	private String path;
	
	private Long record_id;
	
	private String type;
	
	@JsonIgnore
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker; 
}
