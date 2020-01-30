package org.portalengine.portal.Tracker.Status;

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
@RequestMapping("/trackers/status")
public class TrackerStatusController {

	@Autowired
	private TrackerService service;

	@Autowired
	private FileLinkService fileservice;

	private DataSource datasource;

	private JdbcTemplate jdbctemplate;


	@Autowired
	public TrackerStatusController(DataSource datasource) {
		this.datasource = datasource;
		this.jdbctemplate = new JdbcTemplate(datasource);
	}

	@GetMapping("/{tracker_id}")
	public String fields_list(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		return "tracker/status/list.html";
	}

	@GetMapping("/{tracker_id}/create")
	public String create_status(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_status", new TrackerStatus());
		return "tracker/status/form.html";
	}

	@GetMapping("/{tracker_id}/edit/{field_id}")
	public String create_status(@PathVariable Long tracker_id, @PathVariable Long field_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerStatus field = service.getStatusRepo().getOne(field_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_status", field);
		return "tracker/status/form.html";
	}

	@PostMapping("/{tracker_id}/delete/{field_id}")
	public String delete_status(@PathVariable Long tracker_id, @PathVariable Long field_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerStatus field = service.getStatusRepo().getOne(field_id);
		if(field!=null) {
			tracker.remove(field);
			service.getStatusRepo().deleteById(field_id);
		}
		return "redirect:/trackers/status/" + tracker_id.toString();
	}

	@PostMapping("/{tracker_id}/save")
	public String save_status(@PathVariable Long tracker_id,@Valid TrackerStatus tracker_status, Model model) {

		Tracker tracker = service.getRepo().getOne(tracker_id);
		tracker.add(tracker_status);

		service.getStatusRepo().save(tracker_status);
		return "redirect:/trackers/status/" + tracker_id.toString();
	}
}