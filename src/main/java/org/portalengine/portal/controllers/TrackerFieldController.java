package org.portalengine.portal.controllers;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerField;
import org.portalengine.portal.services.FileLinkService;
import org.portalengine.portal.services.TrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/trackers/fields")
public class TrackerFieldController {

	@Autowired
	private TrackerService service;

	@Autowired
	private FileLinkService fileservice;

	private DataSource datasource;

	private JdbcTemplate jdbctemplate;


	@Autowired
	public TrackerFieldController(DataSource datasource) {
		this.datasource = datasource;
		this.jdbctemplate = new JdbcTemplate(datasource);
	}

	@GetMapping("/{tracker_id}")
	public String fields_list(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getById(tracker_id);
		model.addAttribute("pageTitle","Field Listing - " + tracker.getName());
		model.addAttribute("tracker", tracker);
		return "tracker/field/list.html";
	}

	@GetMapping(value= {"/{tracker_id}/create","/{tracker_id}/edit/{field_id}"})
	public String form(@PathVariable Long tracker_id, Model model,@PathVariable(required=false) Long field_id) {
		Tracker tracker = service.getRepo().getById(tracker_id);
		if(field_id!=null) {
			TrackerField field = service.getFieldRepo().getById(field_id);
			model.addAttribute("pageTitle","Edit Field - " + field.getName());
			model.addAttribute("tracker_field", field);
		}
		else {
			model.addAttribute("pageTitle","Create Field - " + tracker.getName());
			TrackerField tf = new TrackerField();
			tf.setOptionSource("[]");
			model.addAttribute("tracker_field", tf);
		}		
		model.addAttribute("tracker", tracker);
		return "tracker/field/form.html";
	}

	@PostMapping("/{tracker_id}/delete/{field_id}")
	public String delete_field(@PathVariable Long tracker_id, @PathVariable Long field_id, Model model) {
		Tracker tracker = service.getRepo().getById(tracker_id);
		TrackerField field = service.getFieldRepo().getById(field_id);
		if(field!=null) {
			tracker.remove(field);
			service.getFieldRepo().deleteById(field_id);
		}
		return "redirect:/admin/trackers/fields/" + tracker_id.toString();
	}

	@PostMapping("/{tracker_id}/save")
	public String save_field(@PathVariable Long tracker_id,@Valid TrackerField tracker_field, Model model) {

		Tracker tracker = service.getRepo().getById(tracker_id);
		tracker.add(tracker_field);

		service.getFieldRepo().save(tracker_field);
		return "redirect:/admin/trackers/fields/" + tracker_id.toString();
	}
}
