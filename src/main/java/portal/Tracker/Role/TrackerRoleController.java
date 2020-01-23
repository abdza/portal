package portal.Tracker.Role;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import portal.FileLink.FileLinkService;
import portal.Tracker.Tracker;
import portal.Tracker.TrackerService;

@Controller
@RequestMapping("/trackers/roles")
public class TrackerRoleController {

	@Autowired
	private TrackerService service;

	@Autowired
	private FileLinkService fileservice;

	private DataSource datasource;

	private JdbcTemplate jdbctemplate;


	@Autowired
	public TrackerRoleController(DataSource datasource) {
		this.datasource = datasource;
		this.jdbctemplate = new JdbcTemplate(datasource);
	}

	@GetMapping("/{tracker_id}")
	public String fields_list(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		return "tracker/role/list.html";
	}

	@GetMapping("/{tracker_id}/create")
	public String create_status(@PathVariable Long tracker_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_role", new TrackerRole());
		return "tracker/role/form.html";
	}

	@GetMapping("/{tracker_id}/edit/{role_id}")
	public String create_status(@PathVariable Long tracker_id, @PathVariable Long role_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerRole field = service.getRoleRepo().getOne(role_id);
		model.addAttribute("tracker", tracker);
		model.addAttribute("tracker_role", field);
		return "tracker/role/form.html";
	}

	@PostMapping("/{tracker_id}/delete/{role_id}")
	public String delete_status(@PathVariable Long tracker_id, @PathVariable Long role_id, Model model) {
		Tracker tracker = service.getRepo().getOne(tracker_id);
		TrackerRole field = service.getRoleRepo().getOne(role_id);
		if(field!=null) {
			tracker.remove(field);
			service.getRoleRepo().deleteById(role_id);
		}
		return "redirect:/trackers/roles/" + tracker_id.toString();
	}

	@PostMapping("/{tracker_id}/save")
	public String save_role(@PathVariable Long tracker_id,@Valid TrackerRole tracker_role, Model model) {

		Tracker tracker = service.getRepo().getOne(tracker_id);
		tracker.add(tracker_role);

		service.getRoleRepo().save(tracker_role);
		return "redirect:/trackers/roles/" + tracker_id.toString();
	}
}
