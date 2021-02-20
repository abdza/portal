package org.portalengine.portal.Tracker.Field;

import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.Tracker.Tracker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tracker_field")
public class TrackerField extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String name;
	
	@NotNull
	private String label;
	
	private String fieldType;
	private String fieldWidget;
	
	@org.hibernate.annotations.Type( type = "text" )
	private String optionSource;
	
	@JsonIgnore
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tracker_id" )
	private Tracker tracker;
	
	@Transient
	public String[] reserved_name = {"user","id","order","by","group","date","select","from","where","asc","desc","ct"};
	
	@Transient
	public String[] typeOptions = {"String","Text","Integer","Number","Date","DateTime","Checkbox","TreeNode","TrackerType","User","HasMany"};
	
	@Transient
	public String[] widgetOptions = {"Default","DropDown"};
	
	public TrackerField copy(Tracker destTracker) {
		TrackerField newfield = new TrackerField();
		newfield.name = this.name;
		newfield.label = this.label;
		newfield.fieldType = this.fieldType;
		newfield.fieldWidget = this.fieldWidget;
		newfield.optionSource = this.optionSource;
		newfield.tracker = destTracker;
		return newfield;
	}
	

	
	public String display(SqlRowSet datas) {
		if(datas!=null) {
			if(fieldType.equals("String") || fieldType.equals("Text")) {
				return datas.getString(name);
			}
			else if(fieldType.equals("Integer")||fieldType.equals("User")||fieldType.equals("TreeNode")||fieldType.equals("TrackerType")) {
				return String.valueOf(datas.getInt(name));
			}
			else if(fieldType.equals("Number")) {
				return String.valueOf(datas.getDouble(name));
			}
			else if(fieldType.equals("Date")) {
				return datas.getDate(name).toString();
			}
			else if(fieldType.equals("DateTime")) {
				return datas.getDate(name).toString();
			}
		}
		return "";
	}
	
	public String typeClass() {
		String toreturn = "";
		if(this.fieldType.equals("Date")) {
			toreturn = " datepicker datetimepicker-input";
		}
		else if(this.fieldType.equals("DateTime")) {
			toreturn = " datetimepicker datetimepicker-input ";
		}
		return toreturn;
	}
	
	public JsonNode optionsJson() {
		ObjectMapper mapper = new ObjectMapper();
	    JsonNode qjson = null;
	    if(this.optionSource!=null && this.optionSource.length()>0) {
			try {				
				qjson = mapper.readTree(this.optionSource.replace('`', '"'));				
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return qjson;
	}
	
	@PreRemove
	void beforeDelete() {
		System.out.println("This is going to remove the " + this.name + " field");
	}
}
