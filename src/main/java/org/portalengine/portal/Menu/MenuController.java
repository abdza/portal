package org.portalengine.portal.Menu;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.TrackerService;
import org.portalengine.portal.Tree.TreeService;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Controller
@RequestMapping("/menus")
public class MenuController {
	
		@Autowired
		private MenuService service;
		
		@Autowired
		public MenuController() {
		}

		@GetMapping
		public String list(HttpServletRequest request, Model model) {
			int page = 0;
			int size = 10;
			if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
				page = Integer.parseInt(request.getParameter("page"));
			}
			if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
				size = Integer.parseInt(request.getParameter("size"));
			}
			model.addAttribute("PageTitle","Menu Listing");
			model.addAttribute("menus", service.getMenuRepo().findAll(PageRequest.of(page, size)));
			return "menu/list.html";
		}
		
		@GetMapping(value= {"/create","/edit/{id}"})
		public String edit(@PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				Menu curMenu = service.getMenuRepo().getOne(id);
				model.addAttribute("PageTitle","Edit Menu - " + curMenu.getTitle());
				model.addAttribute("menu", curMenu);
			}
			else {
				model.addAttribute("PageTitle","Create Menu");
				model.addAttribute("menu", new Menu());
			}
			
			return "menu/form.html";
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Menu curMenu = service.getMenuRepo().getOne(id);
			model.addAttribute("PageTitle",curMenu.getTitle());
			model.addAttribute("menu", curMenu);
			return "menu/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Menu Menu,Model model) {
			service.getMenuRepo().save(Menu);
			return "redirect:/menus/display/" + Menu.getId().toString();
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.getMenuRepo().deleteById(id);
			return "redirect:/menus";
		}
}

