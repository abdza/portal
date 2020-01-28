package org.portalengine.portal.DataUpdate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiConsumer;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.User.User;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_data_update")
public class DataUpdate {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker;
	
	@OneToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "filelink_id" )
	private FileLink filelink;
	
	private Long batchNo;
	private Long uploadStatus;
	private Long dataRow;
	private Long dataEnd;
	private Long headerStart;
	private Long headerEnd;
	
	private String fileName;
	private String filePath;
	
	private Date dateCreated;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id" )
	private User user;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String remarks;	
	
	@org.hibernate.annotations.Type( type = "text" )
	private String savedParams;	
	
	@org.hibernate.annotations.Type( type = "text" )
	private String messages;
	
	@PrePersist
	void updateDateCreated() {
		this.dateCreated = new Date();
	}
}
