package org.portalengine.portal.Module;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.TrackerService;
import org.portalengine.portal.Tree.TreeService;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.support.Repositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Controller
@RequestMapping("/modules")
public class ModuleController {
	
		@Autowired
		private ModuleService service;
		
		@Autowired
		public ModuleController() {
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
			model.addAttribute("pageTitle","Module Listing");
			model.addAttribute("modules", service.getRepo().findAll(PageRequest.of(page, size)));
			return "module/list.html";
		}
		
		@GetMapping("/update")
		public String update(Model model) {
			System.out.println("Updating modules list");
			service.updatelisting();
			return "redirect:/modules";
		}
		
		@PostMapping("/export/{id}")
		public String export(@PathVariable Long id, Model model) {
			Module module = service.getRepo().getOne(id);
			service.exportModule(module.getName());
			return "redirect:/modules";
		}
		
		@PostMapping("/import/{id}")
		public String importModule(@PathVariable Long id, Model model) {
			Module module = service.getRepo().getOne(id);
			service.importModule(module.getName());
			return "redirect:/modules";
		}
}
