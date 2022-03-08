package org.portalengine.portal.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.portalengine.portal.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_data_update")
public class DataUpdate extends Auditable<String> {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker;
	
	@OneToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "filelink_id" , columnDefinition = "numeric(19,0)" )
	@Type(type = "big_decimal")
	private FileLink filelink;
	
	private Long uploadStatus;
	private Long dataRow;
	private Long dataEnd;
	private Long headerStart;
	private Long headerEnd;
	
	private String fileName;
	private String filePath;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id", columnDefinition = "numeric(19,0)"  )
	private User user;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String remarks;	
	
	@org.hibernate.annotations.Type( type = "text" )
	private String savedParams;	
	
	@org.hibernate.annotations.Type( type = "text" )
	private String messages;	
}
