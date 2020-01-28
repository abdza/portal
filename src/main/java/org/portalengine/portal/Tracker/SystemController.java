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
	private PageService pageservice;
	
	@Autowired
	private FileLinkService fileservice;
	
	@Autowired
	private TrackerService trackerservice;
	
	@Autowired
	public SystemController() {
	}
	
	@GetMapping("/{module}/{slug}/transition/{transition_id}/{data_id}")
	public String createdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long transition_id, @PathVariable Long data_id, Model model) {
		Tracker tracker = trackerservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			TrackerTransition transition = trackerservice.getTransitionRepo().getOne(transition_id);
			model.addAttribute("trackerservice",trackerservice);
			model.addAttribute("tracker", tracker);
			model.addAttribute("transition",transition);
			String formtitle = tracker.getTitle() + " " + transition.getName();
			model.addAttribute("formtitle",formtitle);
			HashMap<String,Object> datarow = trackerservice.datarow(tracker, data_id);
			model.addAttribute("datas", datarow);
			Page pp = pageservice.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_" + trackerservice.slugify(transition.getName()));
			if(pp!=null) {
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerservice);
				ctx2.put("tracker", tracker);
				ctx2.put("datas", datarow);
				ctx2.put("transition",transition);
				ctx2.put("formtitle",formtitle);
				String content = pageservice.getTemplateFromMap(pp.getContent(), ctx2);
				model.addAttribute("content", content);
				pp.setContent(content);
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
		Tracker tracker = trackerservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("trackerservice",trackerservice);
			model.addAttribute("tracker", tracker);
			model.addAttribute("formtitle","New " + tracker.getTitle());
			model.addAttribute("transition",trackerservice.create_transition(tracker));
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/display/{id}")
	public String displaydata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("trackerservice",trackerservice);
			model.addAttribute("tracker", tracker);
			HashMap<String,Object> datarow = trackerservice.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			Page pp = pageservice.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_display");
			if(pp!=null) {
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerservice);
				ctx2.put("tracker", tracker);
				ctx2.put("datas", datarow);
				String content = pageservice.getTemplateFromMap(pp.getContent(), ctx2);
				model.addAttribute("content", content);
				pp.setContent(content);
				return "page/plain.html";
			}
			return "tracker/data/display.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/edit/{id}")
	public String editdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("trackerservice",trackerservice);
			model.addAttribute("tracker", tracker);
			String formtitle = "Edit " + tracker.getTitle();
			model.addAttribute("formtitle",formtitle);
			HashMap<String,Object> datarow = trackerservice.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			Page pp = pageservice.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_edit");
			if(pp!=null) {
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerservice);
				ctx2.put("tracker", tracker);
				ctx2.put("datas", datarow);
				ctx2.put("formtitle", formtitle);
				String content = pageservice.getTemplateFromMap(pp.getContent(), ctx2);
				model.addAttribute("content", content);
				pp.setContent(content);
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
		Tracker tracker = trackerservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			
			trackerservice.saveForm(tracker,(User)authentication.getPrincipal());
			Map<String, String[]> postdata = request.getParameterMap();
			if(postdata.get("transition_id")!=null) {
				TrackerTransition transition = trackerservice.getTransitionRepo().getOne(Long.parseLong(postdata.get("transition_id")[0]));
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
		Tracker tracker = trackerservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("trackerservice",trackerservice);
			model.addAttribute("tracker", tracker);
			String listtitle = tracker.getTitle();
			model.addAttribute("listtitle",listtitle);
			Page pp = pageservice.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_list");
			if(pp!=null) {
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerservice);
				ctx2.put("tracker", tracker);
				ctx2.put("listtitle", listtitle);
				String content = pageservice.getTemplateFromMap(pp.getContent(), ctx2);
				model.addAttribute("content", content);
				pp.setContent(content);
				return "page/plain.html";
			}
			return "tracker/data/list.html";
		}
		else {
			return "404";
		}
	}
}
