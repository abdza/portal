package org.portalengine.portal.Tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	
}
