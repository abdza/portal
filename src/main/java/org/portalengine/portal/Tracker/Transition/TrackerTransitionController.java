package org.portalengine.portal.Tracker.Transition;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
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
		return "tracker/transition/list.html";
	}

	@GetMapping("/{tracker_id}/create")
	public String create_status(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_transition", new TrackerTransition());
		return "tracker/transition/form.html";
	}

	@GetMapping("/{tracker_id}/edit/{role_id}")
	public String create_status(@PathVariable Long tracker_id, @PathVariable Long role_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerTransition field = service.getTransitionRepo().getOne(role_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_transition", field);
		return "tracker/transition/form.html";
	}

	@PostMapping("/{tracker_id}/delete/{role_id}")
	public String delete_status(@PathVariable Long tracker_id, @PathVariable Long role_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerTransition field = service.getTransitionRepo().getOne(role_id);
		if(field!=null) {
			tracker.remove(field);
			service.getTransitionRepo().deleteById(role_id);
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
