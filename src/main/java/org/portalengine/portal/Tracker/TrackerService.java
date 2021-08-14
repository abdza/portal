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
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.PortalPage;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Setting.SettingService;
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
import org.portalengine.portal.User.Role.UserRole;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
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
	private SettingService settingService;
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	@Autowired
	private DataUpdateService dataUpdateService;
	
	@Value("${spring.datasource.url}")
    private String dataURL;
	
	/* Read application.properties with the following function:
	 * String keyValue = env.getProperty(key);
	 */
	@Autowired
	private Environment env;
	
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
			jdbctemplate.execute(dquery);
		}
	}
	
	public String css_head(Tracker tracker, String list_name) {
		List<TrackerField> fields = field_list(tracker,list_name,null);
		List<String> toreturn = new ArrayList<String>();
		int count = 2;
		toreturn.add(".responsive td:nth-of-type(1):before { content:'#'; }");
		for(TrackerField field:fields) {
			toreturn.add(".responsive td:nth-of-type(" + String.valueOf(count) + "):before { content:'" + field.getLabel() + "'; }");
			count++;
		};
		return "@media only screen and (max-width: 760px),(min-device-width: 768px) and (max-device-width: 1024px)  { " + String.join(" ",toreturn) + "}";
	}
	
	public List<TrackerField> field_list(Tracker tracker, String list_name, TrackerTransition transition) {
		switch(list_name) {
		case "filter":
			return fields(tracker,tracker.getFilterFields());
		case "excel":
			if(tracker.getExcelFields()!=null && tracker.getExcelFields().length()>0) {
				return fields(tracker,tracker.getExcelFields());
			}
			else {			
				return fields(tracker,tracker.getListFields());
			}
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
		if(fieldnames!=null) {
			for(String fname:fieldnames.split(",")) {
				TrackerField tf = fieldRepo.findByTrackerAndName(tracker, fname);
				if(tf!=null) {
					toreturn.add(tf);
				}
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
	
	public List<HashMap<String,String>> filter_options(TrackerField field) {
		ArrayList<HashMap<String,String>> toret = new ArrayList<HashMap<String,String>>();
		
		MapSqlParameterSource paramsource = new MapSqlParameterSource();		
		SqlRowSet options = namedjdbctemplate.queryForRowSet("select distinct " + field.getName() + " from " + field.getTracker().getDataTable() + " order by " + field.getName(), paramsource);
		HashMap<String,String> dtoin = new HashMap<String,String>();
		dtoin.put("val", "All");
		dtoin.put("label", "All");
		toret.add(dtoin);
		while(options.next()) {
			if(options.getString(field.getName())!=null) {
				String opt = options.getString(field.getName());
				HashMap<String,String> toin = new HashMap<String,String>();
				toin.put("val", opt);
				if(field.getFieldType().equals("TrackerType")) {
					JsonNode foptions = field.optionsJson();
					if(foptions.get("module")!=null && foptions.get("slug")!=null && foptions.get("name_column")!=null) {
						String module = foptions.get("module").textValue();
						String slug = foptions.get("slug").textValue();
						String name_column = foptions.get("name_column").textValue();
						Tracker targetTracker = repo.findOneByModuleAndSlug(module, slug);					
						if(targetTracker!=null) {
							HashMap<String,Object> dataobj = datarow(targetTracker,Long.valueOf(options.getString(field.getName())));
							if(dataobj!=null) {
								toin.put("val", opt);
								opt = String.valueOf(dataobj.get(name_column));
							}
						}
					}
				}
				toin.put("label", opt);
				toret.add(toin);
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
	
	public HashMap<String,Object> datarow(String module, String slug, LinkedHashMap<String,Object> search) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			return datarow(tracker,search);
		}
		else {
			return null;
		}
	}
	
	public HashMap<String,Object> datarow(Tracker tracker, LinkedHashMap<String,Object> search) {
		
		Object[] results = hashMapData(tracker,search,false).getDataRows();
		if(results.length>0) {	
			return (HashMap<String,Object>)results[0];
		}
		return null;
		
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
				// If tracker is actually a tracker, return record status even when not asked for it
				currow.put("record_status", toret.getObject("record_status"));
			}
		}

		return currow;
	}
	
	public SqlRowSet customquery(String query) {
		return customquery(query,null);
	}
	
	public SqlRowSet customquery(String query, Map<String, Object> mapdata) {
		CustomQuery cquery = new CustomQuery(namedjdbctemplate);
		if(mapdata!=null) {
			mapdata.forEach((fieldname,fieldval)->{
				cquery.addValue(fieldname,fieldval);			
			});	
		}
		try {
			SqlRowSet qheaders = cquery.query(query);		
			qheaders.next();
			return qheaders;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public DataSet dataset(Tracker tracker) {
		return dataset(tracker, true);
	}
	
	public DataSet dataset(Tracker tracker, boolean pagelimit) {
		/* Default function for tracker to get dataset to display */
		
		ObjectMapper mapper = new ObjectMapper();
		boolean gotfields = false;		
		ObjectNode filterjson = mapper.createObjectNode();		
		ObjectNode qjson = mapper.createObjectNode();
		
		/* Filtering based on the filter fields of the tracker */
		List<TrackerField> filters = field_list(tracker,"filter",null);
		ObjectNode fjson = mapper.createObjectNode();
		ArrayNode equalNode = fjson.putArray("equal");
		/* Looping for each filter */
		filters.forEach(filter->{			
			/* Check to see if the parameter is set for that filter using the opt_<filtername> parameter */
			if(request.getParameter("opt_" + filter.getName())!=null) {				
				String opt = request.getParameter("opt_" + filter.getName());
				/* Make sure that the selection is actually valid data and that it is not 'All'.
				 * 'All' option will bypass this filter
				 */
				if(opt.length()>0 && !opt.equals("All")) {					
					ObjectNode fojson = mapper.createObjectNode();
					fojson.put(filter.getName(), opt);
					/* Add the filtering field and value into the default json query 
					 * for 'equal' values
					 * */
					equalNode.add(fojson);
				}
			}
		});
		
		/* Check to see if the tracker is filtering based on search	 */
		if(request.getParameter("q")!=null) {
			String q = request.getParameter("q");
			ObjectNode inqjson = mapper.createObjectNode();
			// {"q":"ahmad","like":["name","description"]}				
			ArrayNode arrayNode = inqjson.putArray("like");
			if(tracker.getSearchFields()!=null) {
				/* Loop over searchfields split by comma array */
				for(String sfield:tracker.getSearchFields().split(",")) {
					if(sfield.length()>0) {
						/* If value got length, add the field to the node to be test for like */
						arrayNode.add(sfield);
						gotfields = true;
					}
				}
				if(gotfields) {
					if(q.length()>0) {
						inqjson.put("q", q);
						qjson.put("or",inqjson);
					}
					else {
						gotfields = false;
					}
				}
			}
		}
		
		/* Got both search query and filter query */
		if(gotfields && equalNode.size()>0) {
			/* So got to filter by filter AND search */
			ArrayNode andNode = filterjson.putArray("and");
			andNode.add(fjson);
			andNode.add(qjson);
		}
		/* but if only got search query only */
		else if(gotfields) {
			/* then only filter by search json */
			filterjson = qjson;
		}
		/* only got filter */
		else if(equalNode.size()>0) {
			ArrayNode andNode = filterjson.putArray("and");
			andNode.add(fjson);
		}
		
		if(filterjson.size()>0) {
			return dataset(tracker, filterjson, pagelimit);
		}
		else {
			return dataset(tracker, null, pagelimit);	
		}
		
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
	
	public String role_query(Tracker tracker, User user) {
		List<String> dquery = new ArrayList<String>();		
		for(TrackerRole tr:tracker.getRoles()) {
			if(tr.getRoleType().equals("Data Compare")) {
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();												
				binding.setVariable("request", request);
				binding.setVariable("postdata", postdata);
				binding.setVariable("trackerService",this);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);				
				binding.setVariable("settingService", settingService);				
				binding.setVariable("env", env);
				String content = null;
				try {
					content = (String) shell.evaluate(tr.getRoleRule());
					if(content!=null && content.length()>0) {
						dquery.add(" (" + content + ") ");
					}
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
			}
		}
		if(dquery.size()>0) {
			return "(" + String.join(" or ",dquery) + ")";
		}
		else {
			return "";
		}
	}
	
	public DataSet dataset(Tracker tracker, JsonNode search, boolean pagelimit) {
		DataSet dataset = new DataSet();
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		String basequery = "select * from " + tracker.getDataTable() + " ct ";
		String filterquery = "";
		String orderby = null;
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
			curquery = jsonquery(tracker, search, null, paramsource,"or");
			orderby = (String) curquery.get("orderby");
			filterquery = (String) curquery.get("filterquery");
			
			if(filterquery!=null && filterquery.length()>0) {
				filterquery = " where " + filterquery;
			}
			paramsource = (MapSqlParameterSource) curquery.get("paramsource");			
		}
		String userfilter = " 1=1 ";
		
		User curuser = userService.currentUser();
		List<UserRole> mr = tracker.module_roles(curuser);		
		if(mr.size()==0) {			
			String rq = role_query(tracker,curuser);
			if(rq.length()>0) {
				userfilter = rq;
			}
			if(filterquery!=null && filterquery.length()>0) {
				filterquery = filterquery + " and " + userfilter;
			}
			else {
				filterquery = " where " + userfilter;
			}
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
		String pagequery = " order by id";
		if(orderby!=null && orderby.length()>1) {
			pagequery = " order by " + orderby + ",id";
		}
		if(pagelimit) {
			pagequery += " offset " + offset.toString() + " rows fetch next " + size.toString() + " rows only";	
		}
		
		if(dataURL.contains("jdbc:mysql")) {
			pagequery = " order by id";
			if(orderby!=null && orderby.length()>1) {
				pagequery = " order by " + orderby + ",id";
			}
			if(pagelimit) {
				pagequery += " limit " + offset.toString() + "," + size.toString();
			}
		}
		String countquery = "select count(*) from " + tracker.getDataTable() + " ct " + filterquery;		
		Integer rowcount = namedjdbctemplate.queryForObject(countquery, paramsource, Integer.class);		
		Integer totalPages = rowcount/size;
		if(rowcount%size>0) {
			totalPages += 1;
		}
		dataset.setTotalPages(totalPages);
		System.out.println("final query:" + basequery + filterquery + pagequery);
		System.out.println(paramsource);
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
	
	public HashMap<String,Object> jsonquery(Tracker tracker, JsonNode search, String prependfilter, MapSqlParameterSource paramsource, String combinor) {		
		HashMap<String,Object> curquery = new HashMap<String,Object>();
		HashMap<String,Object> subquery = new HashMap<String,Object>();
		
		ArrayList<String> squery = new ArrayList<String>();
		String filterquery = "";
		String qstring = null;
		boolean foundquery = false;
		
		if(search.get("and") != null) {
			subquery = jsonquery(tracker, search.get("and"), filterquery, paramsource,"and");
			paramsource = (MapSqlParameterSource) subquery.get("paramsource");
			filterquery = (String) subquery.get("filterquery");
		}
		if(search.get("or") != null) {
			subquery = jsonquery(tracker, search.get("or"), filterquery, paramsource,"or");
			paramsource = (MapSqlParameterSource) subquery.get("paramsource");
			filterquery = (String) subquery.get("filterquery");
		}
		if(search.get("not") != null) {			
			subquery = jsonquery(tracker, search.get("not"), filterquery, paramsource,"not");
			paramsource = (MapSqlParameterSource) subquery.get("paramsource");
			filterquery = " not " + (String) subquery.get("filterquery");
			System.out.println("Got not in the query");
			System.out.println(filterquery);
		}
		if(combinor==null) {
			combinor = "or";
		}
		
		if(search.get("q")!=null) {				
			foundquery = true;
			qstring = search.get("q").asText();
		}		
		if(search.get("like")!=null) {
			foundquery = true;
			String exp = "like"; 
			if(dataURL.contains("jdbc:postgresql")) {
				exp = "ilike";
			}
			if(search.get("like").isArray()) {				
				qstring = "%" + qstring + "%";
				for(final JsonNode jfield : search.get("like")) {
					
					squery.add(" ct." + jfield.asText() + " " + exp + " :" + jfield.asText() + " ");
					paramsource = addValue(tracker,paramsource,jfield.asText(), qstring);
				}
			}
			else if(search.get("like").isObject()) {					
				Iterator<Map.Entry<String,JsonNode>> svals = search.get("like").fields();
				while(svals.hasNext()) {
					Entry<String, JsonNode> node = svals.next();
					squery.add(" ct." + node.getKey() + " " + exp + " :" + node.getKey() + " ");
					paramsource = addValue(tracker,paramsource,node.getKey(),node.getValue().asText());
				}
			}
		}
		if(search.get("equal")!=null) {
			foundquery = true;
			if(search.get("equal").isArray()) {				
				for(final JsonNode jfield : search.get("equal")) {						
					if(jfield.isObject()) {
						Iterator<String> fieldNames = jfield.fieldNames();
						while(fieldNames.hasNext()) {
				            String fieldName = fieldNames.next();
				            String queryval = jfield.get(fieldName).asText();
				            if(queryval==null) {
				            	squery.add(" ct." + fieldName + " is null ");			
				            }
				            else {				            	
								squery.add(" ct." + fieldName + " = :" + fieldName + " ");							
								paramsource = addValue(tracker,paramsource,fieldName, queryval);
				            }
						}						
					}
					else {
						squery.add(" ct." + jfield.asText() + " = :" + jfield.asText() + " ");
						paramsource = addValue(tracker,paramsource,jfield.asText(), qstring);
					}
				}
			}
			else if(search.get("equal").isObject()) {				
				Iterator<Map.Entry<String,JsonNode>> svals = search.get("equal").fields();
				while(svals.hasNext()) {
					Entry<String, JsonNode> node = svals.next();
					String queryval = node.getValue().asText();					
					if(queryval==null || queryval.equals("null")) {
						squery.add(" ct." + node.getKey() + " is null ");
					}
					else {						
						squery.add(" ct." + node.getKey() + " = :" + node.getKey() + " ");
						paramsource = addValue(tracker,paramsource,node.getKey(), queryval);
					}
				}
			}
		}
		if(!foundquery) {
			if(search.isArray()) {
				for(final JsonNode jq : search) {
					subquery = jsonquery(tracker, jq, filterquery, paramsource, combinor);
					paramsource = (MapSqlParameterSource) subquery.get("paramsource");
					filterquery = (String) subquery.get("filterquery");		
				}
			}			
		}
		if(squery.size()>0) {
			String newfilterquery = "( " + String.join(" " + combinor + " ", squery) + " )";
			if(filterquery!=null && filterquery.length()>0) {
				filterquery = filterquery + " " + combinor + " " + newfilterquery;
			}
			else {
				filterquery = newfilterquery;
			}
		}
		if(prependfilter!=null && prependfilter.length()>0) {
			filterquery = prependfilter + " " + combinor + " " + filterquery;
		}
		
		String orderby = null;
		if(search.get("order")!=null) {
			orderby = " ";
			if(search.get("order").isArray()) {
				int curpos = 1;
				for(final JsonNode jfield : search.get("order")) {
					if(curpos>1) {
						orderby += ",";
					}
					orderby += jfield.textValue();
					curpos++;
				}
			}
			else if(search.get("order").isObject()) {
				Iterator<Map.Entry<String,JsonNode>> svals = search.get("order").fields();
				int curpos = 1;
				while(svals.hasNext()) {
					Entry<String, JsonNode> node = svals.next();
					if(curpos>1) {
						orderby += ",";
					}
					orderby += node.getValue().asText();
					curpos++;
				}
			}
			else {
				System.out.println("what on earth " + search.get("order").getClass());
			}			
		}
		curquery.put("orderby", orderby);		
		curquery.put("filterquery", filterquery);
		curquery.put("paramsource", paramsource);
		
		return curquery;
	}
	
	public Object[] dataRows(String module, String slug) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		return hashMapData(tracker,null,false).getDataRows();
	}
	
	public Object[] dataRows(String module, String slug, LinkedHashMap<String,Object> search) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		return hashMapData(tracker,search,false).getDataRows();
	}
	
	public Object[] dataRows(Tracker tracker) {		
		return hashMapData(tracker,null,false).getDataRows();
	}
	
	public Object[] dataRows(Tracker tracker, LinkedHashMap<String,Object> search) {		
		return hashMapData(tracker,search,false).getDataRows();
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
	
	public Long saveMap(String module, String slug,Map<String, Object> mapdata) {
		Tracker tracker = repo.findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			return saveMap(tracker,mapdata);
		}
		else {
			return (long) -1;
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
	
	public MapSqlParameterSource addValue(Tracker tracker, MapSqlParameterSource paramsource, String fieldname ,Object data) {		
		TrackerField tf = fieldRepo.findByTrackerAndName(tracker, fieldname);
		if(tf==null) {			
			if(fieldname.equals("id")) {
				return paramsource.addValue("id",data,Types.NUMERIC);
			}
		}		
		return addValue(paramsource, tf, data);
	}
	
	public MapSqlParameterSource addValue(MapSqlParameterSource paramsource, TrackerField tf,Object data) {
		try {
			if(tf!=null) {
				switch(tf.getFieldType()) {
				case "String":
					paramsource.addValue(tf.getName(), data,Types.VARCHAR);
					break;
				case "Text":
					paramsource.addValue(tf.getName(), data,Types.LONGVARCHAR);
					break;
				case "TrackerType":
				case "TreeNode":
				case "Integer":
				case "User":				
				case "Number":
					paramsource.addValue(tf.getName(),data,Types.NUMERIC);
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
	
					date = format.parse((String) data);
					if(date!=null) {
						if(tf.getFieldType().equals("Date")) {
							paramsource.addValue(tf.getName(), date, Types.DATE);
						}
						else {
							paramsource.addValue(tf.getName(), date, Types.TIMESTAMP);
						}
					}
				}
			}			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return paramsource;
	}
	
	public String currentDate() {
		return dateToString(new Date());
	}
	
	public String dateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return formatter.format(date);
	}
	
	/* To easily save data in tracker using groovy
	 * 
	 * trackerService.saveMap(tracker, ['<field1>':'any data','<field2>':'any data 2'])
	 * 
	 * Method would return the id of the item saved
	 * 
	 */
	public Long saveMap(Tracker tracker,Map<String, Object> mapdata) {
		ArrayList<TrackerField> submittedfields = new ArrayList<TrackerField>();
		ArrayList<String> submittednames = new ArrayList<String>();
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		HashMap<String,Object> curobject = null;
		if(mapdata.get("id")!=null) {
			try {
				curobject = datarow(tracker,Long.parseLong((String) mapdata.get("id")));
			}
			catch(ClassCastException cce) {				
				curobject = datarow(tracker,Long.valueOf(((Integer)mapdata.get("id")).longValue()));
			}
		}
		mapdata.forEach((fieldname,fieldval)->{
			TrackerField tfield = fieldRepo.findByTrackerAndName(tracker,fieldname);
			if(tfield!=null) {
				submittedfields.add(tfield);
			}
		});		
		// using array to get around local variable in enclosing scope need to be final
		MapSqlParameterSource[] inparamsource = {new MapSqlParameterSource()};
		submittedfields.forEach(tf->{
			inparamsource[0] = addValue(inparamsource[0], tf, mapdata.get(tf.getName()));
			submittednames.add(tf.getName());
		});
		paramsource = inparamsource[0];
		String dquery;
		Long curid = null;
		if(mapdata.get("id")!=null) {
			ArrayList<String> updatenames = new ArrayList<String>();
			submittednames.forEach(sname->{				
				if(dataURL.contains("jdbc:mysql")) {
					updatenames.add('`' + sname + '`' + "=:" + sname);
				}
				else if(dataURL.contains("jdbc:postgresql")) {
					updatenames.add('"' + sname + '"' + "=:" + sname);
				}
				else {
					updatenames.add('[' + sname + ']' + "=:" + sname);
				}
			});
			dquery = "update " + tracker.getDataTable() + " set " + String.join(",", updatenames) + " where id=:id";
			try {
				curid = Long.parseLong((String) mapdata.get("id"));
			}
			catch(ClassCastException cce) {				
				curid = Long.valueOf(((Integer)mapdata.get("id")).longValue());
			}
			paramsource.addValue("id", curid);
		}
		else {
			if(dataURL.contains("jdbc:mysql")) {
				dquery = "insert into " + tracker.getDataTable() + " (`" + String.join("`,`", submittednames) + "`) values (:" + String.join(",:" , submittednames) + ")";
			}
			else if(dataURL.contains("jdbc:postgresql")) {			
				dquery = "insert into " + tracker.getDataTable() + " (\"" + String.join("\",\"", submittednames) + "\") values (:" + String.join(",:" , submittednames) + ")";
			}
			else {
				dquery = "insert into " + tracker.getDataTable() + " ([" + String.join("],[", submittednames) + "]) values (:" + String.join(",:" , submittednames) + ")";
			}
		}		
		KeyHolder keyholder = new GeneratedKeyHolder();
		namedjdbctemplate.update(dquery,paramsource,keyholder, new String[] { "id" });
		if(mapdata.get("id")==null) {
			curid = keyholder.getKey().longValue();
		}		
		return curid;
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
					if(ftfields!=null) {   // list of fields from transition is not null
						if(ftfields.contains(tfield)) {
							submittedfields.add(tfield);
							if(fcurobject!=null) {
								changes.add(tfield.getLabel() + " is changed from " + fcurobject.get(tfield.getName()) + " to " + postdata.get("val_" + tfield.getName())[0].toString());
							}
						}
					}
					else {	// list of fields from transition is null (ie just a statement)
						submittedfields.add(tfield);
						if(fcurobject!=null) {
							changes.add(tfield.getLabel() + " is changed from " + fcurobject.get(tfield.getName()) + " to " + postdata.get("val_" + tfield.getName())[0].toString());
						}						
					}
				}
			}
		});
		
		postdata.forEach((fieldname,fieldval)->{   // this time loop again to look for missing fields (ie checkbox hidden fields)
			if(fieldname.length()>5 && fieldname.substring(0, 5).equals("_val_")) {
				String curfieldname = fieldname.substring(5);
				TrackerField tfield = fieldRepo.findByTrackerAndName(tracker,curfieldname);
				if(tfield!=null) {
					if(ftfields!=null) {   // list of fields from transition is not null
						if(ftfields.contains(tfield)) {
							if(!submittedfields.contains(tfield)) {								
								submittedfields.add(tfield);
								if(fcurobject!=null) {
									changes.add(tfield.getLabel() + " is changed from " + fcurobject.get(tfield.getName()) + " to " + postdata.get("_val_" + tfield.getName())[0].toString());
								}
							}
						}
					}
					else { // list of fields from transition is null (ie just a statement)
						if(!submittedfields.contains(tfield)) {
							submittedfields.add(tfield);
							if(fcurobject!=null) {
								changes.add(tfield.getLabel() + " is changed from " + fcurobject.get(tfield.getName()) + " to " + postdata.get("_val_" + tfield.getName())[0].toString());
							}						
						}
					}
				}
			}
		});
		
		submittedfields.forEach(tf->{			
			try {
				String dval=null;
				try {
					dval = postdata.get("val_" + tf.getName())[0];
				} catch (NullPointerException e) {
					dval = postdata.get("_val_" + tf.getName())[0];
				}
				if(dval.equals("auto_field")) {					
					Binding binding = new Binding();		
					GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);					
					binding.setVariable("pageService",pageService);
					binding.setVariable("postdata", postdata);
					binding.setVariable("request", request);
					binding.setVariable("trackerService",this);
					binding.setVariable("treeService",treeService);
					binding.setVariable("userService",userService);
					binding.setVariable("fileService",fileService);
					binding.setVariable("settingService", settingService);
					binding.setVariable("env", env);
					try {
						dval = (String)shell.evaluate(tf.getAutoValue());
						System.out.println("Content:" + dval);
					}
					catch(Exception e) {
						System.out.println("Error in page:" + e.toString());
					}
				}
				
				switch(tf.getFieldType()) {
				case "Checkbox":
					boolean curval;
					if(dval.equals("on")) {
						curval = true;						
					}
					else {
						curval = false;
					}
					paramsource.addValue(tf.getName(), curval, Types.SMALLINT);
					break;
				case "String":
					paramsource.addValue(tf.getName(), dval, Types.VARCHAR);					
					break;
				case "Text":
					paramsource.addValue(tf.getName(), dval, Types.LONGVARCHAR);					
					break;
				case "TrackerType":
				case "TreeNode":
				case "Integer":
				case "User":
					if(dval.length()>0) {
						paramsource.addValue(tf.getName(), Integer.parseInt(dval), Types.NUMERIC);
					}
					else {
						paramsource.addValue(tf.getName(),null);
					}
					break;
				case "Number":
					if(dval.length()>0) {
						paramsource.addValue(tf.getName(), Double.parseDouble(dval), Types.NUMERIC);
					}
					else {
						paramsource.addValue(tf.getName(),null);
					}
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
	
					if(dval.length()>0) {
						date = format.parse(dval);
						if(date!=null) {
							if(tf.getFieldType().equals("Date")) {
								paramsource.addValue(tf.getName(), date, Types.DATE);
							}
							else {
								paramsource.addValue(tf.getName(), date, Types.TIMESTAMP);
							}
						}
					}
					else {
						paramsource.addValue(tf.getName(),null);
					}
	
				}
				submittednames.add(tf.getName());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				System.out.println("In parse error");
				e.printStackTrace();
			} catch (NumberFormatException e) {
				System.out.println("In number exception error");
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
				if(dataURL.contains("jdbc:mysql")) {
					updatenames.add('`' + sname + '`' + "=:" + sname);
				}
				else if(dataURL.contains("jdbc:postgresql")) {
					updatenames.add('"' + sname + '"' + "=:" + sname);
				}
				else {
					updatenames.add('[' + sname + ']' + "=:" + sname);
				}
			});
			dquery = "update " + tracker.getDataTable() + " set " + String.join(",", updatenames) + " where id=:id";
			curid = Long.parseLong(postdata.get("id")[0]);
			paramsource.addValue("id", curid);
		}
		else {
			if(dataURL.contains("jdbc:mysql")) {
				dquery = "insert into " + tracker.getDataTable() + " (`" + String.join("`,`", submittednames) + "`) values (:" + String.join(",:" , submittednames) + ")";
			}
			else if(dataURL.contains("jdbc:postgresql")) {			
				dquery = "insert into " + tracker.getDataTable() + " (\"" + String.join("\",\"", submittednames) + "\") values (:" + String.join(",:" , submittednames) + ")";
			}
			else {
				dquery = "insert into " + tracker.getDataTable() + " ([" + String.join("],[", submittednames) + "]) values (:" + String.join(",:" , submittednames) + ")";
			}
		}
		KeyHolder keyholder = new GeneratedKeyHolder();		
		namedjdbctemplate.update(dquery,paramsource,keyholder, new String[] { "id" });
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
	
	public void executeQuery(String query) {
		try {			
			jdbctemplate.execute(query);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void clearDb(Tracker tracker) {
		jdbctemplate.execute("truncate table " + tracker.getDataTable());
	}
	
	public boolean dbTableExists(String tablename) {
		if(dataURL.contains("jdbc:h2")) {
			tablename = tablename.toUpperCase();			
		}
		String toquery = "select count(*) as result from INFORMATION_SCHEMA.TABLES where "
				+ " TABLE_NAME = '" + tablename + "'";		
		SqlRowSet trythis = jdbctemplate.queryForRowSet(toquery);
		trythis.next();
		if(trythis.getInt("result")==0) {
			System.out.println("Table " + tablename + " does not exists");
			return false;
		}
		System.out.println("Table " + tablename + " already exists");
		return true;		
	}
	
	public boolean dbFieldExists(String tablename, String columname) {
		if(dataURL.contains("jdbc:h2")) {
			tablename = tablename.toUpperCase();			
		}
		String toquery = "select count(*) as result from INFORMATION_SCHEMA.COLUMNS where "
				+ " TABLE_NAME = '" + tablename + "' and COLUMN_NAME = '" + columname + "'";		
		SqlRowSet trythis = jdbctemplate.queryForRowSet(toquery);
		trythis.next();
		if(trythis.getInt("result")==0) {
			System.out.println("Field " + columname + " does not exists");
			return false;
		}
		System.out.println("Field " + columname + " already exists");
		return true;
	}
	
	public String dbEscapeColumn(String columname) {
		if(dataURL.contains("jdbc:mysql")) {
			return "`" + columname + "`";
		}
		else if(dataURL.contains("jdbc:postgresql") || dataURL.contains("jdbc:h2")) {
			return "\"" + columname + "\"";
		} 
		else {
			return "[" + columname + "]";	
		}
	}
	
	public void updateDb(Tracker tracker) {
		
		if(tracker.getDataTable().length()>0) {			
			// Check whether data table already exists
			
			if(!dbTableExists(tracker.getDataTable().toLowerCase())) {
				// Data table does not exists yet, so please create
				if(dataURL.contains("jdbc:mysql")) {
					jdbctemplate.execute("create table " + tracker.getDataTable().toLowerCase() + " (" + dbEscapeColumn("id") + " INT NOT NULL AUTO_INCREMENT,"
							+ " PRIMARY KEY(" + dbEscapeColumn("id") + "))");
				}
				else if(dataURL.contains("jdbc:postgresql")) {
					jdbctemplate.execute("create table if not exists " + tracker.getDataTable().toLowerCase() + " (" + dbEscapeColumn("id") + " serial PRIMARY KEY)");
				}
				else {
					jdbctemplate.execute("create table " + tracker.getDataTable().toLowerCase() + " (" + dbEscapeColumn("id") + " INT NOT NULL IDENTITY(1,1),"
						+ "CONSTRAINT PK_" + tracker.getDataTable().toLowerCase() + UUID.randomUUID().toString().replace("-", "") + " PRIMARY KEY(" + dbEscapeColumn("id") + "))");
				}
			}			
			
			if(!dbFieldExists(tracker.getDataTable().toLowerCase(),"record_status")) {
				// Check to see if column record_status doesn't exist yet
				if(!tracker.getTrackerType().equals("Statement")) {
					// Please add record_status if type is a tracker (ie not a statement)
					jdbctemplate.execute("alter table " + tracker.getDataTable().toLowerCase() + " add " + dbEscapeColumn("record_status") + " varchar(256) NULL");
				}
			}
			
			if(!dbFieldExists(tracker.getDataTable().toLowerCase(),"dataupdate_id")) { 
				jdbctemplate.execute("alter table " + tracker.getDataTable().toLowerCase() + " add " + dbEscapeColumn("dataupdate_id") + " numeric(24,0) NULL");
			}
			if(tracker.getTrackerType().equals("Trailed Tracker")) {
				// Need to check whether need to create updates table
				if(tracker.getUpdatesTable().length()>0) {					
					if(!dbTableExists(tracker.getUpdatesTable().toLowerCase())) {
						// If updates table does not exists please create one
						if(dataURL.contains("jdbc:mysql")) {
							jdbctemplate.execute("create table " + tracker.getUpdatesTable().toLowerCase() + " (id INT NOT NULL AUTO_INCREMENT, "
									+ "attachment_id numeric(19,0), description text, record_id numeric(19,0),"
									+ "update_date datetime, updater_id numeric(19,0), status varchar(255),"
									+ "changes text, allowedroles varchar(255),CONSTRAINT PK_" + tracker.getUpdatesTable().toLowerCase() + UUID.randomUUID().toString().replace("-", "") + " PRIMARY KEY(id))");
						}
						else if(dataURL.contains("jdbc:postgresql")) {
							jdbctemplate.execute("create table " + tracker.getUpdatesTable().toLowerCase() + " (id serial PRIMARY KEY, "
									+ "attachment_id numeric(19,0), description text, record_id numeric(19,0),"
									+ "update_date datetime, updater_id numeric(19,0), status varchar(255),"
									+ "changes text, allowedroles varchar(255),CONSTRAINT PK_" + tracker.getUpdatesTable().toLowerCase() + UUID.randomUUID().toString().replace("-", "") + ")");
						}
						else {
							jdbctemplate.execute("create table " + tracker.getUpdatesTable().toLowerCase() + " (id INT NOT NULL IDENTITY(1,1), "
									+ "attachment_id numeric(19,0), description text, record_id numeric(19,0),"
									+ "update_date datetime, updater_id numeric(19,0), status varchar(255),"
									+ "changes text, allowedroles varchar(255),CONSTRAINT PK_" + tracker.getUpdatesTable().toLowerCase() + UUID.randomUUID().toString().replace("-", "") + " PRIMARY KEY(id))");
						}
					}
				}
			}
			List<TrackerField> fields = fieldRepo.findByTracker(tracker);
			for(TrackerField field: fields) {
				updateDb(field);
			}
		}
	}
	
	public String display(TrackerField field, HashMap<String,Object> datas) {
		try {
			if(datas!=null) {
				Object fdata = datas.get(field.getName());
				if(field.getFieldType().equals("Date") || field.getFieldType().equals("DateTime")) {
					if(fdata!=null) {
						DateFormat format;
						if(field.getFieldType().equals("Date")) {
							format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
						}
						else {
							format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);
						}
						return format.format((Date)fdata);
					}
					else {
						return "";
					}
				}
				else if(field.getFieldType().equals("User")) {
					if(fdata!=null) {
						Long targetid = ((BigDecimal)fdata).longValue() ;
						User data = userService.getRepo().getOne(targetid);
						if(data!=null) {
							return data.getName();
						}
					}
					else {
						return "";
					}
				}
				else if(field.getFieldType().equals("TrackerType")) {				
					JsonNode foptions = field.optionsJson();
					if(foptions.get("module")!=null && foptions.get("slug")!=null && foptions.get("name_column")!=null) {
						if(fdata!=null) {
							String module = foptions.get("module").textValue();
							String slug = foptions.get("slug").textValue();
							String name_column = foptions.get("name_column").textValue();
							Tracker targetTracker = repo.findOneByModuleAndSlug(module, slug);					
							if(targetTracker!=null && fdata!=null) {						
								BigDecimal cdata = (BigDecimal)fdata;						
								if(cdata!=null) {
									Long targetid = cdata.longValue() ;												
									HashMap<String,Object> targetdatas = datarow(targetTracker, targetid);
									if(targetdatas!=null && targetdatas.get(name_column)!=null) {
										return targetdatas.get(name_column).toString();
									}
								}								
							}
						}
						else {
							return "";
						}
					}
				}
				else if(field.getFieldType().equals("HasMany")) {				
					JsonNode foptions = field.optionsJson();
					String toret = "";
					if(foptions.get("module")!=null && foptions.get("slug")!=null && foptions.get("fields")!=null && foptions.get("refer_field")!=null) {					
						String module = foptions.get("module").textValue();
						String slug = foptions.get("slug").textValue();
						String refer_field = foptions.get("refer_field").textValue();					
						Tracker targetTracker = repo.findOneByModuleAndSlug(module, slug);
						if(targetTracker!=null && datas.get("id")!=null) {						
							HashMap<String,Object> curquery = new HashMap<String,Object>();
							LinkedHashMap<String,Object> qq = new LinkedHashMap<String,Object>(curquery);
							Long targetid = Long.valueOf((Integer)datas.get("id"));												
							Object[] targetdatas = dataRows(targetTracker,qq);
							toret += "<table id='data_" + field.getName() + "' class='table'>";
							toret += "<tr><th>#</th>";
							for(final JsonNode jfield : foptions.get("fields")) {														
								TrackerField curf = fieldRepo.findByTrackerAndName(targetTracker, jfield.asText());
								if(curf!=null) {								
									toret += "<th>" + curf.getLabel() + "</th>";
								}
							}
							toret += "</tr>";
							for(int i=0;i<targetdatas.length;i++) {							
								toret += "<tr>";
								toret += "<td>" + String.valueOf(i+1) + "</td>";
								for(final JsonNode jfield : foptions.get("fields")) {															
									HashMap<String,Object> row = (HashMap<String,Object>)targetdatas[i];								
									toret += "<td>" + row.get(jfield.asText()) + "</td>";
								}
								toret += "</tr>";
							}
							toret += "</table>";
						}
					}
					return toret;
				}
				else if(field.getFieldType().equals("Checkbox")) {					
					if(fdata!=null && (fdata.equals("1") || (Integer)fdata==1)) {
						return "true";
					}
					else {
						return "false";
					}
				}
				else {
					if(fdata!=null) {
						return String.valueOf(fdata);
					}
					else {
						return "";
					}
				}
			}
			else {
				return "";
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
		if(field.getFieldType()!=null && field.getFieldType()!="HasMany") {
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
				if(dataURL.contains("jdbc:postgresql")) {
					sqltype = "timestamp";
				}
				else {
					sqltype = "datetime";
				}
				break;
			case "Checkbox":
				sqltype = "smallint";
				break;
			}
		}
		
		if(!dbFieldExists(field.getTracker().getDataTable().toLowerCase(),field.getName().toLowerCase())) {
			String fixsql = "";
			if(dataURL.contains("jdbc:mysql")) {
				fixsql = "alter table " + field.getTracker().getDataTable().toLowerCase() + " add `" + field.getName().toLowerCase() + "` " + sqltype + " NULL";
			}
			else if(dataURL.contains("jdbc:postgresql") || dataURL.contains("jdbc:h2")) {
				fixsql = "alter table " + field.getTracker().getDataTable().toLowerCase() + " add \"" + field.getName().toLowerCase() + "\" " + sqltype + " NULL";
			} 
			else {
				fixsql = "alter table " + field.getTracker().getDataTable().toLowerCase() + " add [" + field.getName().toLowerCase() + "] " + sqltype + " NULL";	
			}			
			jdbctemplate.execute(fixsql);
		}
	}
	
	public boolean listAction(Tracker tracker) {
		return true;
	}
	
	public Tracker load(String module, String slug) {
		return repo.findOneByModuleAndSlug(module, slug);
	}
}
