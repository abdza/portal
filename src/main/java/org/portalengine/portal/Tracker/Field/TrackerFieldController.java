package org.portalengine.portal.Tracker.Field;

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
@RequestMapping("/trackers/fields")
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
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		return "tracker/field/list.html";
	}

	@GetMapping("/{tracker_id}/create")
	public String create_field(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		TrackerField tf = new TrackerField();
		tf.setOptionSource("[]");
		model.addAttribute("tracker_field", tf);
		return "tracker/field/form.html";
	}

	@GetMapping("/{tracker_id}/edit/{field_id}")
	public String create_field(@PathVariable Long tracker_id, @PathVariable Long field_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerField field = service.getFieldRepo().getOne(field_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_field", field);
		return "tracker/field/form.html";
	}

	@PostMapping("/{tracker_id}/delete/{field_id}")
	public String delete_field(@PathVariable Long tracker_id, @PathVariable Long field_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerField field = service.getFieldRepo().getOne(field_id);
		if(field!=null) {
			tracker.remove(field);
			service.getFieldRepo().deleteById(field_id);
		}
		return "redirect:/trackers/fields/" + tracker_id.toString();
	}

	@PostMapping("/{tracker_id}/save")
	public String save_field(@PathVariable Long tracker_id,@Valid TrackerField tracker_field, Model model) {

		Tracker tracker = service.getRepo().getOne(tracker_id);
		tracker.add(tracker_field);

		service.getFieldRepo().save(tracker_field);
		return "redirect:/trackers/fields/" + tracker_id.toString();
	}
}
