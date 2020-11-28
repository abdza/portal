package org.portalengine.portal.Tracker;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.portalengine.portal.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class SystemController {
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	/* Read application.properties with the following function:
	 * String keyValue = env.getProperty(key);
	 */
	@Autowired
	private Environment env;
	
	@Autowired
	public SystemController() {
	}
	
	@GetMapping("/{module}/{slug}/transition/{transition_id}/{data_id}")
	public String createdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long transition_id, @PathVariable Long data_id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			TrackerTransition transition = trackerService.getTransitionRepo().getOne(transition_id);
			model.addAttribute("trackerservice",trackerService);
			model.addAttribute("tracker", tracker);
			model.addAttribute("transition",transition);
			String formtitle = tracker.getName() + " " + transition.getName();
			model.addAttribute("formtitle",formtitle);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, data_id);
			model.addAttribute("datas", datarow);
			Page pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_" + trackerService.slugify(transition.getName()));
			if(pp!=null) {
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/create")
	public String createdata(@PathVariable String module, @PathVariable String slug, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("trackerservice",trackerService);
			model.addAttribute("tracker", tracker);
			String formtitle = "New " + tracker.getName();
			model.addAttribute("formtitle",formtitle);			
			model.addAttribute("pageTitle",formtitle);
			model.addAttribute("transition",trackerService.create_transition(tracker));
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/display/{id}")
	public String displaydata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("tracker", tracker);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			String datatitle = tracker.getName() + " - Details";
			System.out.println("Title:" + datatitle);
			model.addAttribute("datatitle",datatitle);
			model.addAttribute("pageTitle",datatitle);
			Page pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_display");
			if(pp!=null) {
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			return "tracker/data/display.html";
		}
		else {
			return "404";
		}
	}
	
	@PostMapping("/{module}/{slug}/delete/{id}")
	public String deletedata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		paramsource.addValue("id", id);
		namedjdbctemplate.update("delete from " + tracker.getDataTable() + " where id=:id", paramsource);
		return "redirect:/" + tracker.getModule() + "/" + tracker.getSlug() + "/list";
	}
	
	@GetMapping("/{module}/{slug}/edit/{id}")
	public String editdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("tracker", tracker);
			String formtitle = "Edit " + tracker.getName();
			model.addAttribute("formtitle",formtitle);
			model.addAttribute("pageTitle",formtitle);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			Page pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_edit");
			if(pp!=null) {
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@PostMapping("/{module}/{slug}/save")
	public String save(@PathVariable String module, @PathVariable String slug, Model model,Authentication authentication) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			User curuser = null;
			if(authentication!=null) {
				curuser = (User)authentication.getPrincipal();
			}
			
			trackerService.saveForm(tracker,curuser);
			Map<String, String[]> postdata = request.getParameterMap();
			if(postdata.get("transition_id")!=null) {
				TrackerTransition transition = trackerService.getTransitionRepo().getOne(Long.parseLong(postdata.get("transition_id")[0]));
			}
			if(postdata.get("id")!=null) {
				return "redirect:/" + tracker.getModule() + "/" + tracker.getSlug() + "/display/" + postdata.get("id")[0].toString();
			}
			else {			
				return "redirect:/" + tracker.getModule() + "/" + tracker.getSlug() + "/list";
			}
		} 
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/list")
	public String list(@PathVariable String module, @PathVariable String slug, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		model.addAttribute("tracker", tracker);
		String listtitle = tracker.getName();
		model.addAttribute("listtitle",listtitle);
		model.addAttribute("pageTitle",listtitle);
		Page pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_list");
		if(pp!=null) {
			model.addAttribute("page",pp);
			model.addAttribute("content", pp.getContent());				
			return "page/plain.html";
		}
		return "tracker/data/list.html";
	}
}
