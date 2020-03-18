package org.portalengine.portal.Tracker.File;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.Tracker.Tracker;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tracker_file")
public class TrackerFile extends Auditable<String> {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
		
	private String name;
	
	private String path;
	
	private Long record_id;
	
	private String type;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker; 
}
