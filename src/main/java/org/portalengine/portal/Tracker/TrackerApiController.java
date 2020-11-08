package org.portalengine.portal.Tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.UserApiController;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;

@RestController
@RequestMapping("/api/trackers")
public class TrackerApiController {
	@Autowired
	private TrackerService service;
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/{tracker_id}/fields")
	public Object fieldsList(@PathVariable Long tracker_id,HttpServletRequest request, Model model) {
			Tracker tracker = service.getRepo().getOne(tracker_id);
			List<TrackerField> fields = service.getFieldRepo().findByTracker(tracker);
			ArrayList<Map<String,String>> jfields = new ArrayList<Map<String,String>>();
			fields.forEach(cfield->{
				Map<String,String> map = new HashMap<String,String>();
				map.put("id",cfield.getName());
				map.put("name",cfield.getName());
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
	
	@GetMapping("/query_field/{field_id}")
	public Object queryField(@PathVariable Long field_id, HttpServletRequest request, Model model) {
		Map<String, Object> map = new HashMap<String,Object>();
		
		TrackerField field = service.getFieldRepo().getOne(field_id);
		
		if(field!=null) {
		
			String search = "%";
			DataSet toreturn = null;
			JsonNode optJson = field.optionsJson();
			
			if(optJson!=null && optJson.get("query")!=null) {
				ObjectNode onode = (ObjectNode) optJson.get("query");
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
				map.put("data", datalist);
			}
		}
		return map;
	}
	
}
