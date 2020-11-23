package org.portalengine.portal.Tracker;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.DataUpdate.DataUpdateService;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Field.TrackerFieldRepository;
import org.portalengine.portal.Tracker.Role.TrackerRole;
import org.portalengine.portal.Tracker.Role.TrackerRoleRepository;
import org.portalengine.portal.Tracker.Status.TrackerStatus;
import org.portalengine.portal.Tracker.Status.TrackerStatusRepository;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.portalengine.portal.Tracker.Transition.TrackerTransitionRepository;
import org.portalengine.portal.Tree.TreeNode;
import org.portalengine.portal.Tree.TreeService;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.UserService;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

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
	private TreeService treeService;
	
	@Autowired
	private TrackerTransitionRepository transitionRepo;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	@Autowired
	private DataUpdateService dataUpdateService;
	
	@Value("${spring.datasource.url}")
    private String dataURL;
	
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
			String activefields=null;
			if(transition!=null) {
				activefields = transition.getEditFields();
			}
			if(activefields==null || activefields=="") {
				activefields = tracker.getFormFields();
			}
			if(activefields==null || activefields=="") {
				activefields = tracker.getListFields();
			}
			if(activefields!=null && activefields.length()>0) {
				return fields(tracker,activefields);
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
	
	public boolean deletable(Tracker tracker, Integer id) {
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
	
	public void deleteById(Long id) {
		Tracker tracker = repo.findById(id).orElse(null);
		if(tracker!=null) {
			dataUpdateService.deleteUpdateByTracker(tracker);
			
			SqlRowSet trytrails = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.TABLES where "
					+ "TABLE_SCHEMA = 'dbo' and TABLE_NAME = '" + tracker.getDataTable()+ "'");
			if(trytrails.next()) {
				SqlRowSet intrytrails = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.TABLES where "
						+ "TABLE_SCHEMA = 'dbo' and TABLE_NAME = 'deleted_" + tracker.getDataTable()+ "'");
				if(intrytrails.next()) {
					jdbctemplate.execute("drop table deleted_" + tracker.getDataTable());
				}
				jdbctemplate.execute("exec sp_rename '" + tracker.getDataTable() + "', 'deleted_" + tracker.getDataTable() + "'");
			}
			if(tracker.getTrackerType().equals("Trailed Tracker")) {
				trytrails = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.TABLES where "
						+ "TABLE_SCHEMA = 'dbo' and TABLE_NAME = '" + tracker.getUpdatesTable() + "'");
				if(trytrails.next()) {
					SqlRowSet intrytrails = jdbctemplate.queryForRowSet("select * from INFORMATION_SCHEMA.TABLES where "
							+ "TABLE_SCHEMA = 'dbo' and TABLE_NAME = 'deleted_" + tracker.getUpdatesTable() + "'");
					if(intrytrails.next()) {
						jdbctemplate.execute("drop table deleted_" + tracker.getUpdatesTable());
					}
					jdbctemplate.execute("exec sp_rename '" + tracker.getUpdatesTable() + "', 'deleted_" + tracker.getUpdatesTable() + "'");
				}
			}			
			
			repo.deleteById(id);
		}
	}
	
	public List<HashMap<String,String>> field_options(TrackerField field) {
		ArrayList<HashMap<String,String>> toret = new ArrayList<HashMap<String,String>>();
		if(field.getFieldWidget().equals("DropDown")) {			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode savefield;
			try {
				savefield = mapper.readTree(field.getOptionSource());			
				System.out.println("savefield is:" + savefield.toString());
				if(savefield.isArray()) {
					ArrayNode opts = (ArrayNode) savefield;
					for(int i=0;i<opts.size();i++) {
						HashMap<String,String> toin = new HashMap<String,String>();
						toin.put("val", savefield.get(i).asText());
						toin.put("label", savefield.get(i).asText());
						toret.add(toin);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return toret;
	}
	
	public HashMap<String,Object> datarow(String module, String slug, Long id) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			return datarow(tracker,id);
		}
		else {
			return null;
		}
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
		return dataset(tracker, null, true);
	}
	
	public DataSet dataset(String module, String slug) {
		return dataset(module, slug, null);
	}
	
	public DataSet dataset(String module, String slug, String json) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		return dataset(tracker, json);
	}
	
	public DataSet dataset(Tracker tracker, String json) {
		ObjectMapper mapper = new ObjectMapper();
	    JsonNode qjson = null;
	    if(json!=null) {
			try {				
				qjson = mapper.readTree(json.replace('`', '"'));				
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		if(qjson!=null) {
			return dataset(tracker, qjson, false);
		}
		else {
			return dataset(tracker,null,false);
		}
	}
	
	public HashMap<String,Object> jsonquery(JsonNode search, String prependfilter, MapSqlParameterSource paramsource, String combinor) {
		HashMap<String,Object> curquery = new HashMap<String,Object>();
		HashMap<String,Object> subquery = new HashMap<String,Object>();
		
		ArrayList<String> squery = new ArrayList<String>();
		String filterquery = null;
		String qstring = null;		
		
		if(search.get("and") != null) {
			subquery = jsonquery(search.get("and"), filterquery, paramsource,"and");
			paramsource = (MapSqlParameterSource) subquery.get("paramsource");
			filterquery = (String) subquery.get("filterquery");
		}
		if(search.get("or") != null) {
			subquery = jsonquery(search.get("or"), filterquery, paramsource,"or");
			paramsource = (MapSqlParameterSource) subquery.get("paramsource");
			filterquery = (String) subquery.get("filterquery");
		}				
		if(combinor==null) {
			combinor = "or";
		}
		
		if(search.get("q")!=null) {				
			qstring = search.get("q").asText();
		}		
		if(search.get("like")!=null) {
			System.out.println("Got a like");
			if(search.get("like").isArray()) {					
				qstring = "%" + qstring + "%";
				for(final JsonNode jfield : search.get("like")) {						
					squery.add(" " + jfield.asText() + " like :" + jfield.asText() + " ");
					paramsource.addValue(jfield.asText(), qstring);
				}
			}
			else if(search.get("like").isObject()) {					
				Iterator<Map.Entry<String,JsonNode>> svals = search.get("like").fields();
				while(svals.hasNext()) {
					Entry<String, JsonNode> node = svals.next();
					squery.add(" " + node.getKey() + " like :" + node.getKey() + " ");
					paramsource.addValue(node.getKey(),node.getValue().asText());
				}
				/* svals.forEachRemaining( node -> {						
					squery.add(" " + node.getKey() + " like :" + node.getKey() + " ");
					paramsource.addValue(node.getKey(),node.getValue().asText());
				});*/
			}
		}
		if(search.get("equal")!=null) {
			if(search.get("equal").isArray()) {
				for(final JsonNode jfield : search.get("equal")) {						
					squery.add(" " + jfield.asText() + " = :" + jfield.asText() + " ");
					paramsource.addValue(jfield.asText(), qstring);
				}
			}
			else if(search.get("equal").isObject()) {						
				Iterator<Map.Entry<String,JsonNode>> svals = search.get("equal").fields();
				while(svals.hasNext()) {
					Entry<String, JsonNode> node = svals.next();
					squery.add(" " + node.getKey() + " like :" + node.getKey() + " ");
					paramsource.addValue(node.getKey(),node.getValue().asText());
				}
				/* svals.forEachRemaining( node -> {						
					squery.add(" " + node.getKey() + " = :" + node.getKey() + " ");
					paramsource.addValue(node.getKey(),node.getValue().asText());
				}); */
			}
		}
		if(squery.size()>0) {
			String newfilterquery = "(" + String.join(" " + combinor + " ", squery) + ")";
			if(filterquery!=null) {
				filterquery = filterquery + " " + combinor + " " + newfilterquery;
			}
			else {
				filterquery = newfilterquery;
			}
		}
		if(prependfilter!=null) {
			filterquery = prependfilter + " " + combinor + " " + filterquery;
		}
		
		curquery.put("filterquery", filterquery);
		curquery.put("paramsource", paramsource);
		
		return curquery;
	}
	
	public DataSet hashMapData(String module, String slug, LinkedHashMap<String,Object> search) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		return hashMapData(tracker,search,false);
	}
	
	public DataSet hashMapData(String module, String slug, LinkedHashMap<String,Object> search, boolean pagelimit) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		return hashMapData(tracker,search,pagelimit);
	}
	
	public DataSet hashMapData(Tracker tracker, LinkedHashMap<String,Object> search, boolean pagelimit) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode qjson = null;
		try {
			qjson = mapper.readTree(mapper.writeValueAsString(search));
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataset(tracker,qjson,pagelimit);
	}
	
	public DataSet dataset(Tracker tracker, JsonNode search, boolean pagelimit) {
		DataSet dataset = new DataSet();
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		String basequery = "select * from " + tracker.getDataTable();
		System.out.println("basequery:" + basequery);
		String filterquery = "";
		if(search!=null) {
			/*
			 * search is a JsonNode with the following children
			 * q - the query to search for
			 * like - array node of fields to search for using the like query
			 * equals - array node of fields to search for using the equals query
			 * 
			 * All of it would be or
			 * 
			 * Example of valid json
			 * 
			 * {"q":"ahmad","like":["name","description"]}
			 * 
			 * The above would generate the following where query
			 * 
			 * where name like '%ahmad%' or description like '%ahmad%'
			 * 
			 * {"q":"ahmad","equal":["name"]}
			 * 
			 * The above would generate the following
			 * 
			 * where name='ahmad'
			 * 
			 * {"like":{"name":"ahmad","description":"balik"}}
			 * 
			 * The above would generate
			 * 
			 * where name like '%ahmad%' or description like '%balik%'
			 * 
			 * {"equal":{"name":"ahmad","description":"siap"}}
			 * 
			 * The above would generate the following
			 * 
			 * where name='ahmad' or description='siap' 
			 * 
			 * 
			 */
			
			HashMap<String,Object> curquery = new HashMap<String,Object>();
			curquery = jsonquery(search, null, paramsource,"or");
			filterquery = " where 1=1 and " +  (String) curquery.get("filterquery");
			paramsource = (MapSqlParameterSource) curquery.get("paramsource");
			System.out.println("filterquery:" + filterquery);
		}
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
		if(dataURL.contains("jdbc:mysql")) {
			pagequery = " order by id limit " + offset.toString() + "," + size.toString();
		}
		if(!pagelimit) {
			pagequery = "";
		}
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
	
	public int saveMap(String module, String slug,Map<String, Object> mapdata) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			return saveMap(tracker,mapdata);
		}
		else {
			return -1;
		}
	}
	
	public Map<String,Object> flattenMap(Map<String,Object> mapdata) {
		HashMap<String,Object> curobject = new HashMap<String,Object>();
		mapdata.forEach((fieldname,fieldval)->{
			if(fieldval.getClass().isArray()) {
				curobject.put(fieldname, Array.get(fieldval,0));
			}
			else {
				curobject.put(fieldname, fieldval);
			}
		});
		return curobject;
	}
	
	public int saveMap(Tracker tracker,Map<String, Object> mapdata) {
		System.out.println("In savemap");
		ArrayList<TrackerField> submittedfields = new ArrayList<TrackerField>();
		ArrayList<String> submittednames = new ArrayList<String>();
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		HashMap<String,Object> curobject = null;
		if(mapdata.get("id")!=null) {
			curobject = datarow(tracker,Long.parseLong((String) mapdata.get("id")));
		}
		mapdata.forEach((fieldname,fieldval)->{
			// System.out.println("fname:" + fieldname + " fval:" + String.valueOf(fieldval));
			TrackerField tfield = fieldRepo.findByTrackerAndName(tracker,fieldname);
			if(tfield!=null) {
				// System.out.println("tfield:" + tfield.getName());
				submittedfields.add(tfield);
			}
		});
		System.out.println("before submitted");
		submittedfields.forEach(tf->{
			try {
				// System.out.println("processing:" + tf.getName());
				switch(tf.getFieldType()) {
				case "String":
				case "Text":
					paramsource.addValue(tf.getName(), mapdata.get(tf.getName()),Types.VARCHAR);
					break;
				case "TrackerType":
				case "TreeNode":
				case "Integer":
				case "User":
					paramsource.addValue(tf.getName(), mapdata.get(tf.getName()),Types.NUMERIC);
					break;
				case "Number":
					paramsource.addValue(tf.getName(), mapdata.get(tf.getName()),Types.NUMERIC);
					break;
				case "Date":
				case "DateTime":
					DateFormat format;
					if(tf.getFieldType().equals("Date")) {
						format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
					}
					else {
						format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);
					}
					Date date;
	
					date = format.parse((String) mapdata.get(tf.getName()));
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
		// System.out.println("after added paramsource");
		String dquery;
		Long curid = null;
		if(mapdata.get("id")!=null) {
			ArrayList<String> updatenames = new ArrayList<String>();
			submittednames.forEach(sname->{
				updatenames.add(sname + "=:" + sname);
			});
			dquery = "update " + tracker.getDataTable() + " set " + String.join(",", updatenames) + " where id=:id";
			curid = Long.parseLong((String) mapdata.get("id"));
			paramsource.addValue("id", curid);
		}
		else {
			dquery = "insert into " + tracker.getDataTable() + " (" + String.join(",", submittednames) + ") values (:" + String.join(",:" , submittednames) + ")";
		}
		/* System.out.println("dquery:" + dquery);
		System.out.println("params:" + paramsource.toString()); */
		KeyHolder keyholder = new GeneratedKeyHolder();
		namedjdbctemplate.update(dquery,paramsource,keyholder);
		if(mapdata.get("id")==null) {
			curid = keyholder.getKey().longValue();
		}
		return 1;
	}
	
	public long saveForm(Tracker tracker,User principal) {
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
				case "TrackerType":
				case "TreeNode":
				case "Integer":
				case "User":
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
						format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);
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
		/* System.out.println("dquery:" + dquery);
		System.out.println("params:" + paramsource.toString()); */
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
		
		return curid;
	}
	
	public void clearDb(Tracker tracker) {
		jdbctemplate.execute("truncate table " + tracker.getDataTable());
	}
	
	public void updateDb(Tracker tracker) {
		
		if(tracker.getDataTable().length()>0) {
			/* System.out.println("Checking existance of table:" + tracker.getDataTable());
			System.out.println("DC:" + dataURL); */
			// Check whether data table already exists
			String toquery = "select count(*) as result from INFORMATION_SCHEMA.TABLES where "
					+ " TABLE_NAME = '" + tracker.getDataTable().toUpperCase() + "'";
			SqlRowSet trythis = jdbctemplate.queryForRowSet(toquery);
			trythis.next();
			if(trythis.getInt("result")==0) {
				// Data table does not exists yet, so please create
				if(dataURL.contains("jdbc:mysql")) {
					jdbctemplate.execute("create table " + tracker.getDataTable().toUpperCase() + " (ID INT NOT NULL AUTO_INCREMENT,"
							+ " PRIMARY KEY(ID))");
				}
				else {
					jdbctemplate.execute("create table " + tracker.getDataTable().toUpperCase() + " (ID INT NOT NULL IDENTITY(1,1),"
						+ "CONSTRAINT PK_" + tracker.getDataTable().toUpperCase() + UUID.randomUUID().toString().replace("-", "") + " PRIMARY KEY(ID))");
				}
			}
			trythis = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.COLUMNS where "
					+ " TABLE_NAME = '" + tracker.getDataTable().toUpperCase() + "' and COLUMN_NAME = 'RECORD_STATUS'");
			trythis.next();
			if(trythis.getInt("result")==0) {
				// Check to see if column record_status doesn't exist yet
				if(!tracker.getTrackerType().equals("Statement")) {
					// Please add record_status if type is a tracker (ie not a statement)
					jdbctemplate.execute("alter table " + tracker.getDataTable().toUpperCase() + " add RECORD_STATUS varchar(256) NULL");
				}
			}
			trythis = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.COLUMNS where "
					+ " TABLE_NAME = '" + tracker.getDataTable().toUpperCase() + "' and COLUMN_NAME = 'DATAUPDATE_ID'");
			trythis.next();
			if(trythis.getInt("result")==0) {
				jdbctemplate.execute("alter table " + tracker.getDataTable().toUpperCase() + " add DATAUPDATE_ID numeric(24,0) NULL");
			}
			// System.out.println("Type is:" + tracker.getTrackerType() + "-----------------");
			if(tracker.getTrackerType().equals("Trailed Tracker")) {
				// Need to check whether need to create updates table
				if(tracker.getUpdatesTable().length()>0) {
					SqlRowSet trytrails = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.TABLES where "
							+ " TABLE_NAME = '" + tracker.getUpdatesTable().toUpperCase() + "'");
					trytrails.next();
					if(trytrails.getInt("result")==0) {
						// If updates table does not exists please create one
						jdbctemplate.execute("create table " + tracker.getUpdatesTable().toUpperCase() + " (ID INT NOT NULL IDENTITY(1,1), "
								+ "ATTACHMENT_ID numeric(19,0), DESCRIPTION text, RECORD_ID numeric(19,0),"
								+ "UPDATE_DATE datetime, UPDATER_ID numeric(19,0), STATUS varchar(255),"
								+ "CHANGES text, ALLOWEDROLES varchar(255),CONSTRAINT PK_" + tracker.getUpdatesTable().toUpperCase() + UUID.randomUUID().toString().replace("-", "") + " PRIMARY KEY(ID))");
					}
					}
			}
			for(TrackerField field: tracker.getFields()) {
				updateDb(field);
			}
		}
	}
	
	public String display(TrackerField field, HashMap<String,Object> datas) {
		try {
			System.out.println("In display field service");
			if(field.getFieldType().equals("Date") || field.getFieldType().equals("DateTime")) {
				DateFormat format;
				if(field.getFieldType().equals("Date")) {
					format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
				}
				else {
					format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);
				}
				return format.format((Date)datas.get(field.getName()));
			}
			else if(field.getFieldType().equals("User")) {
				Long targetid = ((BigDecimal)datas.get(field.getName())).longValue() ;
				User data = userService.getRepo().getOne(targetid);
				if(data!=null) {
					return data.getName();
				}
			}
			else if(field.getFieldType().equals("TrackerType")) {
				System.out.println("Got trackertype");
				JsonNode foptions = field.optionsJson();
				if(foptions.get("module")!=null && foptions.get("slug")!=null && foptions.get("name_column")!=null) {
					String module = foptions.get("module").textValue();
					String slug = foptions.get("slug").textValue();
					String name_column = foptions.get("name_column").textValue();
					System.out.println("Got module:" + module + " and slug:" + slug);
					Tracker targetTracker = repo.findOneByModuleAndSlug(module, slug);					
					if(targetTracker!=null) {
						System.out.println("Not null for:" + field.getName());
						System.out.println("Val:" + datas.get(field.getName()));
						Long targetid = ((BigDecimal)datas.get(field.getName())).longValue() ;
						System.out.println("Got targettracker searching for:" + targetid.toString());						
						HashMap<String,Object> targetdatas = datarow(targetTracker, targetid);
						if(targetdatas!=null) {
							System.out.println("Got rows from data");
							return targetdatas.get(name_column).toString();
						}
					}
				}
			}
			else {
				return String.valueOf(datas.get(field.getName()));
			}
		}
		catch(Exception exp) {
			System.out.println("Error:" + exp);
			return null;
		}
		return null;
	}
	
	public void updateDb(TrackerField field) {
		String sqltype = "varchar(256)";
		if(field.getFieldType()!=null) {
			switch(field.getFieldType()) {
			case "String":			 
				break;
			case "Text":
				sqltype = "text";
				break;
			case "TrackerType":
			case "TreeNode":
			case "User":
			case "Integer":
				sqltype = "numeric(24,0)";
				break;
			case "Number":
				sqltype = "decimal(24,6)";
				break;
			case "Date":
				sqltype = "date";
				break;
			case "DateTime":
				sqltype = "datetime";
				break;
			case "Checkbox":
				sqltype = "bit";
				break;
			}
		}
		SqlRowSet trythis = jdbctemplate.queryForRowSet("select count(*) as result from INFORMATION_SCHEMA.COLUMNS where "
				+ "TABLE_NAME = '" + field.getTracker().getDataTable().toUpperCase() + "' and COLUMN_NAME = '" + field.getName().toUpperCase() + "'");
		trythis.next();
		if(trythis.getInt("result")==0) {
			jdbctemplate.execute("alter table " + field.getTracker().getDataTable().toUpperCase() + " add " + field.getName().toUpperCase() + " " + sqltype + " NULL");
		}
	}
	
	public boolean listAction(Tracker tracker) {
		return true;
	}
	
	public Tracker load(String module, String slug) {
		return repo.findOneByModuleAndSlug(module, slug);
	}
}
