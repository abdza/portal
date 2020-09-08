package org.portalengine.portal.Tracker.Transition;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
import org.portalengine.portal.Tracker.Role.TrackerRole;
import org.portalengine.portal.Tracker.Status.TrackerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trackers/transitions")
public class TrackerTransitionController {

	@Autowired
	private TrackerService service;

	@Autowired
	private FileLinkService fileservice;

	private DataSource datasource;

	private JdbcTemplate jdbctemplate;


	@Autowired
	public TrackerTransitionController(DataSource datasource) {
		this.datasource = datasource;
		this.jdbctemplate = new JdbcTemplate(datasource);
	}

	@GetMapping("/{tracker_id}")
	public String fields_list(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("pageTitle","Tracker Listing - " + tracker.getName());
		return "tracker/transition/list.html";
	}

	@GetMapping(value= {"/{tracker_id}/create","/{tracker_id}/edit/{transition_id}"})
	public String form(@PathVariable Long tracker_id, Model model,@PathVariable(required=false) Long transition_id) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		if(transition_id!=null) {
			TrackerTransition transition = service.getTransitionRepo().getOne(transition_id);
			model.addAttribute("pageTitle","Edit Transition - " + transition.getName());
			model.addAttribute("tracker_transition", transition);	
		}
		else {
			model.addAttribute("pageTitle","Create Transition - " + tracker.getName());
			model.addAttribute("tracker_transition", new TrackerTransition());
		}
		model.addAttribute("tracker", tracker);
		
		return "tracker/transition/form.html";
	}

	@PostMapping("/{tracker_id}/delete/{transition_id}")
	public String delete_status(@PathVariable Long tracker_id, @PathVariable Long transition_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerTransition field = service.getTransitionRepo().getOne(transition_id);
		if(field!=null) {
			tracker.remove(field);
			service.getTransitionRepo().deleteById(transition_id);
		}
		return "redirect:/trackers/transitions/" + tracker_id.toString();
	}

	@PostMapping("/{tracker_id}/save")
	public String save_role(@PathVariable Long tracker_id,@Valid TrackerTransition tracker_transition, Model model) {

		Tracker tracker = service.getRepo().getOne(tracker_id);
		tracker.add(tracker_transition);

		service.getTransitionRepo().save(tracker_transition);
		return "redirect:/trackers/transitions/" + tracker_id.toString();
	}
}
