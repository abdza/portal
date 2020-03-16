package org.portalengine.portal.Tracker;

import java.security.Principal;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Field.TrackerFieldRepository;
import org.portalengine.portal.Tracker.Role.TrackerRoleRepository;
import org.portalengine.portal.Tracker.Status.TrackerStatus;
import org.portalengine.portal.Tracker.Status.TrackerStatusRepository;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.portalengine.portal.Tracker.Transition.TrackerTransitionRepository;
import org.portalengine.portal.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class TrackerService {
	
	@Autowired
	private HttpServletRequest request;

	@Autowired
	private TrackerRepository repo;
	
	@Autowired
	private TrackerFieldRepository fieldRepo;
	
	@Autowired
	private TrackerStatusRepository statusRepo;
	
	@Autowired
	private TrackerRoleRepository roleRepo;
	
	@Autowired
	private TrackerTransitionRepository transitionRepo;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	@Autowired
	public TrackerService() {
	}
	
	public String slugify(String data) {
		return data.replaceAll("[^A-Za-z0-9]", "_").toLowerCase();
	}
	
	public TrackerTransition create_transition(Tracker tracker) {
		return transitionRepo.createTransition(tracker);
	}
	
	public void FixStatus(Tracker tracker) {
		if(tracker.getInitialStatus().length()>0) {
			StringJoiner statuslist = new StringJoiner(",");
			for(TrackerStatus tstat:tracker.getStatuses()) {
				statuslist.add("'" + tstat.getName() + "'");
			}
			String dquery = "update " + tracker.getDataTable() + " set record_status='" + tracker.getInitialStatus() + "' where record_status not in (" + statuslist.toString() + ") or record_status is null";
			System.out.println(dquery);
			jdbctemplate.execute(dquery);
		}
	}
	
	public List<TrackerField> field_list(Tracker tracker, String list_name, TrackerTransition transition) {
		switch(list_name) {
		case "list":
			return fields(tracker,tracker.getListFields());
		case "form":
			if(transition==null) {
				if(tracker.getFormFields()!=null && tracker.getFormFields().length()>0) {
					return fields(tracker,tracker.getFormFields());
				}
				else {
					return fields(tracker,tracker.getListFields());
				}
			}
			else {
				return fields(tracker,transition.getEditFields());
			}
		case "display":
			if(tracker.getDisplayFields()!=null && tracker.getDisplayFields().length()>0) {
				return fields(tracker,tracker.getDisplayFields());
			}
			else {
				return fields(tracker,tracker.getListFields());
			}
		}
		return null;
	}
	
	public boolean editable(Tracker tracker, Integer id) {
		if(tracker.getTrackerType().equals("Statement")) {
			return true;
		}
		return false;
	}
	
	public List<TrackerTransition> record_transitions(Tracker tracker, Long id) {
		List<TrackerTransition> toreturn = new ArrayList<TrackerTransition>();
		HashMap<String,Object> currow = datarow(tracker,id);
		for(TrackerTransition t:tracker.getTransitions()) {
			if(currow.get("record_status") != null && t.getPrevStatus() != null && currow.get("record_status").equals(t.getPrevStatus())){
				toreturn.add(t);
			}
		}
		return toreturn;
	}
	
	public List<TrackerField> fields(Tracker tracker, String fieldnames) {
		List<TrackerField> toreturn=new ArrayList<TrackerField>();
		for(String fname:fieldnames.split(",")) {
			TrackerField tf = fieldRepo.findByTrackerAndName(tracker, fname);
			if(tf!=null) {
				toreturn.add(tf);
			}
		}
		return toreturn;
	}
	
	public HashMap<String,Object> datarow(Tracker tracker, Long id) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		paramsource.addValue("id", id);
		SqlRowSet toret = namedjdbctemplate.queryForRowSet("select * from " + tracker.getDataTable() + " where id=:id", paramsource);
	
		HashMap<String,Object> currow = new HashMap<String,Object>();
		while(toret.next()) {
			currow.put("id", toret.getObject("id"));
			boolean foundstatus = false;
			for(TrackerField tf:tracker.getFields()) {
				currow.put(tf.getName(), toret.getObject(tf.getName()));
				if(tf.getName().equals("record_status")) {
					foundstatus = true;
				}
			}
			if(!foundstatus && !tracker.getTrackerType().equals("Statement")) {
				currow.put("record_status", toret.getObject("record_status"));
			}
		}

		return currow;
	}
	
	public DataSet dataset(Tracker tracker) {
		DataSet dataset = new DataSet();
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		String basequery = "select * from " + tracker.getDataTable();
		String filterquery = " where 1=1";
		Integer size = 10;
		Integer page = 0;
		if(request.getParameter("page")!=null) {
			page = Integer.parseInt(request.getParameter("page"));
			dataset.setNumber(page);
		}
		if(request.getParameter("size")!=null) {
			size = Integer.parseInt(request.getParameter("size"));
		}
		Integer offset = page * size;
		String pagequery = " order by id offset " + offset.toString() + " rows fetch next " + size.toString() + " rows only";
		Integer rowcount = namedjdbctemplate.queryForObject("select count(*) from " + tracker.getDataTable() + filterquery, paramsource, Integer.class); 
		Integer totalPages = rowcount/size;
		if(rowcount%size>0) {
			totalPages += 1;
		}
		dataset.setTotalPages(totalPages);
		SqlRowSet toret = namedjdbctemplate.queryForRowSet(basequery + filterquery + pagequery, paramsource);
		ArrayList<HashMap<String,Object>> rows = new ArrayList<HashMap<String,Object>>(); 
		while(toret.next()) {
			HashMap<String,Object> datarow = new HashMap<String,Object>();
			for(TrackerField tf:tracker.getFields()) {
				datarow.put(tf.getName(),toret.getObject(tf.getName()));
			}
			datarow.put("id", toret.getObject("id"));
			rows.add(datarow);
		}
		dataset.setNumber(page);
		dataset.setDataRows(rows.toArray());
		return dataset;
	}
	
	public boolean saveForm(Tracker tracker,User principal) {
		Map<String, String[]> postdata = request.getParameterMap();
		ArrayList<TrackerField> submittedfields = new ArrayList<TrackerField>();
		ArrayList<String> submittednames = new ArrayList<String>();
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		TrackerTransition transition = null;
		List<TrackerField> tfields = null;
		HashMap<String,Object> curobject = null;
		ArrayList<String> changes = new ArrayList<String>();
		
		if(postdata.get("id")!=null) {
			curobject = datarow(tracker,Long.parseLong(postdata.get("id")[0]));
		}
		
		if(postdata.get("transition_id")!=null) {
			transition = transitionRepo.getOne(Long.parseLong(postdata.get("transition_id")[0]));
			tfields = fields(tracker, transition.getEditFields());			
		}
		
		final List<TrackerField> ftfields = tfields;
		
		final HashMap<String,Object> fcurobject = curobject;
		postdata.forEach((fieldname,fieldval)->{
			if(fieldname.length()>4 && fieldname.substring(0, 4).equals("val_")) {
				String curfieldname = fieldname.substring(4);
				TrackerField tfield = fieldRepo.findByTrackerAndName(tracker,curfieldname);
				if(tfield!=null) {
					if(ftfields!=null) {
						if(ftfields.contains(tfield)) {
							submittedfields.add(tfield);
							if(fcurobject!=null) {
								changes.add(tfield.getLabel() + " is changed from " + fcurobject.get(tfield.getName()) + " to " + postdata.get("val_" + tfield.getName())[0].toString());
							}
						}
					}
					else {						
						submittedfields.add(tfield);
						if(fcurobject!=null) {
							changes.add(tfield.getLabel() + " is changed from " + fcurobject.get(tfield.getName()) + " to " + postdata.get("val_" + tfield.getName())[0].toString());
						}						
					}
				}
			}
		});
		
		submittedfields.forEach(tf->{
			try {
				switch(tf.getFieldType()) {
				case "String":
				case "Text":
					paramsource.addValue(tf.getName(), postdata.get("val_" + tf.getName())[0],Types.VARCHAR);
					break;
				case "Integer":
					paramsource.addValue(tf.getName(), Integer.parseInt(postdata.get("val_" + tf.getName())[0]),Types.NUMERIC);
					break;
				case "Number":
					paramsource.addValue(tf.getName(), Double.parseDouble(postdata.get("val_" + tf.getName())[0]),Types.NUMERIC);
					break;
				case "Date":
				case "DateTime":
					DateFormat format;
					if(tf.getFieldType().equals("Date")) {
						format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
					}
					else {
						format = new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.ENGLISH);
					}
					Date date;
	
					date = format.parse(postdata.get("val_" + tf.getName())[0]);
					if(date!=null) {
						if(tf.getFieldType().equals("Date")) {
							paramsource.addValue(tf.getName(), date, Types.DATE);
						}
						else {
							paramsource.addValue(tf.getName(), date, Types.TIMESTAMP);
						}
					}
	
				}
				submittednames.add(tf.getName());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		});
		
		if(transition!=null) {
			if(transition.getNextStatus()!=null) {
				paramsource.addValue("record_status", transition.getNextStatus());
				submittednames.add("record_status");
				changes.add("Status is now " + transition.getNextStatus());
			}
		}
		
		String dquery;
		Long curid = null;
		if(postdata.get("id")!=null) {
			ArrayList<String> updatenames = new ArrayList<String>();
			submittednames.forEach(sname->{
				updatenames.add(sname + "=:" + sname);
			});
			dquery = "update " + tracker.getDataTable() + " set " + String.join(",", updatenames) + " where id=:id";
			curid = Long.parseLong(postdata.get("id")[0]);
			paramsource.addValue("id", curid);
		}
		else {
			dquery = "insert into " + tracker.getDataTable() + " (" + String.join(",", submittednames) + ") values (:" + String.join(",:" , submittednames) + ")";
		}
		System.out.println("dquery:" + dquery);
		KeyHolder keyholder = new GeneratedKeyHolder();
		namedjdbctemplate.update(dquery,paramsource,keyholder);
		if(postdata.get("id")==null) {
			curid = keyholder.getKey().longValue();
		}
		
		if(tracker.getTrackerType().equals("Trailed Tracker")) {
			dquery = "insert into " + tracker.getUpdatesTable() + " (update_date,record_id,updater_id,description,changes,allowedroles,status) values (CURRENT_TIMESTAMP,:record_id,:updater_id,:description,:changes,:allowedroles,:status)";
			MapSqlParameterSource tparam = new MapSqlParameterSource();
			tparam.addValue("update_date", new Date());
			tparam.addValue("record_id", curid);
			if(principal!=null) {
				tparam.addValue("updater_id", principal.getId() );
			}
			tparam.addValue("description", String.join("\n\r", changes));
			tparam.addValue("changes", String.join("\n\r", changes));
			tparam.addValue("allowedroles", null);
			tparam.addValue("status", transition.getNextStatus());
			namedjdbctemplate.update(dquery, tparam);			
		}
		
		return true;
	}
}
