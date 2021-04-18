package org.portalengine.portal.Tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Setting.SettingService;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Role.TrackerRole;
import org.portalengine.portal.Tracker.Status.TrackerStatus;
import org.portalengine.portal.Tree.TreeService;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.UserApiController;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.Data;

@RestController
@RequestMapping("/api/trackers")
public class TrackerApiController {
	@Autowired
	private TrackerService service;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private TreeService treeService;
	
	@Autowired
	private SettingService settingService;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private Environment env;
	
	@GetMapping("/{tracker_id}/fields")
	public Object fieldsList(@PathVariable Long tracker_id,HttpServletRequest request, Model model) {
			Tracker tracker = service.getRepo().getOne(tracker_id);
			List<TrackerField> fields = service.getFieldRepo().findByTrackerAndNameContainingIgnoreCase(tracker,request.getParameter("q"));
			ArrayList<Map<String,String>> jfields = new ArrayList<Map<String,String>>();
			fields.forEach(cfield->{
				Map<String,String> map = new HashMap<String,String>();
				map.put("id",cfield.getName());
				map.put("name",cfield.getName());
				jfields.add(map);
			});
			return jfields;
	}
	
	@GetMapping("/{tracker_id}/roles")
	public Object rolesList(@PathVariable Long tracker_id,HttpServletRequest request, Model model) {
			Tracker tracker = service.getRepo().getOne(tracker_id);
			List<TrackerRole> roles = service.getRoleRepo().findByTrackerAndNameContainingIgnoreCase(tracker,request.getParameter("q"));
			ArrayList<Map<String,String>> jfields = new ArrayList<Map<String,String>>();
			String[] userroles = {"None","All","Authenticated"};
			
			for(String cfield:userroles) {
				Map<String,String> map = new HashMap<String,String>();
				map.put("id",cfield);
				map.put("name",cfield);
				jfields.add(map);
			};
			
			roles.forEach(cfield->{
				Map<String,String> map = new HashMap<String,String>();
				map.put("id",cfield.getName());
				map.put("name",cfield.getName());
				jfields.add(map);
			});
			return jfields;
	}
	
	@GetMapping("/{tracker_id}/status")
	public Object statusList(@PathVariable Long tracker_id,HttpServletRequest request, Model model) {
			Tracker tracker = service.getRepo().getOne(tracker_id);
			System.out.println("status function");
			List<TrackerStatus> statuslist = service.getStatusRepo().findByTrackerAndNameContainingIgnoreCase(tracker,request.getParameter("q"));
			ArrayList<Map<String,String>> jfields = new ArrayList<Map<String,String>>();
			System.out.println("found:" + String.valueOf(statuslist.size()));
			statuslist.forEach(cfield->{	
				System.out.println("name:" + cfield.getName());
				Map<String,String> map = new HashMap<String,String>();
				map.put("id",cfield.getName());
				map.put("name",cfield.getName());
				jfields.add(map);
			});
			return jfields;
	}
	
	@GetMapping("/{tracker_id}/pages")
	public Object pagesList(@PathVariable Long tracker_id,HttpServletRequest request, Model model) {
			Tracker tracker = service.getRepo().getOne(tracker_id);
			System.out.println("page function");
			String q = "%" + request.getParameter("q") + "%";
			List<org.portalengine.portal.Page.PortalPage> pageslist = pageService.getRepo().findAllByModuleAndQ(tracker.getModule(), q);
			ArrayList<Map<String,String>> jfields = new ArrayList<Map<String,String>>();
			System.out.println("found:" + String.valueOf(pageslist.size()));
			pageslist.forEach(cpage->{	
				System.out.println("name:" + cpage.getTitle());
				Map<String,String> map = new HashMap<String,String>();
				map.put("id",cpage.getSlug());
				map.put("name",cpage.getTitle());
				jfields.add(map);
			});
			return jfields;
	}
	
	@Data
	private class ApiData {
		public ApiData(Long id,String name2) {
			// TODO Auto-generated constructor stub
			this.id=id;
			this.name=name2;
		}
		Long id;
		String name;
	}
	
	@GetMapping(value= {"/query_field/{field_id}","/query_field/{field_id}/{arg1}","/query_field/{field_id}/{arg1}/{arg2}","/query_field/{field_id}/{arg1}/{arg2}/{arg3}","/query_field/{field_id}/{arg1}/{arg2}/{arg3}/{arg4}","/query_field/{field_id}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"})
	public Object queryField(@PathVariable Long field_id, HttpServletRequest request, Model model, @PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		Map<String, Object> map = new HashMap<String,Object>();
		
		TrackerField field = service.getFieldRepo().getOne(field_id);
		
		if(field!=null) {
			String search = "%";
			Boolean gotoptions = false;
			if(field.getOptionSourceType().equals("Groovy")) {
				
				if(request.getParameter("q")!=null) {
					search = "%" + request.getParameter("q").replace(" " , "%") + "%";
				}
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);				
				binding.setVariable("request", request);
				binding.setVariable("trackerService",service);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);				
				binding.setVariable("settingService", settingService);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				binding.setVariable("arg1", arg1);
				binding.setVariable("arg2", arg2);
				binding.setVariable("arg3", arg3);
				binding.setVariable("arg4", arg4);
				binding.setVariable("arg5", arg5);
				
				Object content = null;
				try {
					content = shell.evaluate(field.getOptionSourceGroovy());
					if(content instanceof SqlRowSet) {
						SqlRowSet curcontent = (SqlRowSet)content;
						ArrayList<ApiData> datalist = new ArrayList<ApiData>();						
						while(curcontent.next()) {
							datalist.add(new ApiData(Long.valueOf((Integer)curcontent.getObject(1)),(String)curcontent.getObject(2)));							
						}
						map.put("content", datalist);
						gotoptions = true;
					}
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}					
			}
			
			if(!gotoptions) {				
				DataSet toreturn = null;
				JsonNode optJson = field.optionsJson();
				
				if(optJson!=null && optJson.get("query")!=null) {
					if(request.getParameter("q")!=null) {
						search = "%" + request.getParameter("q").replace(" " , "%") + "%";
					}
					JsonNode qnode = ((ObjectNode)optJson.get("query")).put("q", search);			
									
					Tracker targetTracker = service.getRepo().findOneByModuleAndSlug(optJson.get("module").asText(), optJson.get("slug").asText());
					
					toreturn = service.dataset(targetTracker, qnode, false);
					
					ArrayList<ApiData> datalist = new ArrayList<ApiData>();
					System.out.println("col to get:" + optJson.get("name_column"));
					for(int i=0;i<toreturn.getDataRows().length;i++) {
						HashMap<String,Object> datarow = (HashMap<String, Object>) toreturn.getDataRows()[i];
						datalist.add(new ApiData(Long.valueOf(datarow.get("id").toString()),(String)datarow.get(optJson.get("name_column").asText())));							
					}
					map.put("content", datalist);
				}
			}
		}
		return map;
	}
	
}
