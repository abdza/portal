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
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private TrackerService trackerService;
	
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
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerService);
				ctx2.put("tracker", tracker);
				ctx2.put("datas", datarow);
				ctx2.put("transition",transition);
				ctx2.put("formtitle",formtitle);
				String content = pageService.getTemplateFromMap(pp.getContent(), ctx2);
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
		/* if(tracker!=null) {
			model.addAttribute("trackerservice",trackerService);
			model.addAttribute("tracker", tracker);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			Page pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_display");
			if(pp!=null) {
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerService);
				ctx2.put("tracker", tracker);
				ctx2.put("datas", datarow);
				String content = pageService.getTemplateFromMap(pp.getContent(), ctx2);
				model.addAttribute("content", content);
				pp.setContent(content);
				return "page/plain.html";
			}
			return "tracker/data/display.html";
		} */
		if(tracker!=null) {
			return trackerService.displayData(model, tracker, id);
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/edit/{id}")
	public String editdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		/* if(tracker!=null) {
			model.addAttribute("trackerservice",trackerService);
			model.addAttribute("tracker", tracker);
			String formtitle = "Edit " + tracker.getName();
			model.addAttribute("formtitle",formtitle);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			Page pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_edit");
			if(pp!=null) {
				Map<String, Object> ctx2 = new HashMap<String, Object>();
				ctx2.put("trackerservice",trackerService);
				ctx2.put("tracker", tracker);
				ctx2.put("datas", datarow);
				ctx2.put("formtitle", formtitle);
				String content = pageService.getTemplateFromMap(pp.getContent(), ctx2);
				model.addAttribute("content", content);
				pp.setContent(content);
				return "page/plain.html";
			}
			return "tracker/data/form.html";
		} */
		if(tracker!=null) {
			return trackerService.editData(model, tracker, id);
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
		if(tracker!=null) {
			return trackerService.displayList(model, tracker);
		}
		else {
			return "404";
		}
	}
}
