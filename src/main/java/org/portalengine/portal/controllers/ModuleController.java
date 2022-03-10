package org.portalengine.portal.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.entities.Module;
import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.services.FileLinkService;
import org.portalengine.portal.services.ModuleService;
import org.portalengine.portal.services.TrackerService;
import org.portalengine.portal.services.TreeService;
import org.portalengine.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.Repositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Controller
@RequestMapping("/admin/modules")
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
			
			String search = "";
			Page<Module> toreturn = null;
			if(request.getParameter("q")!=null) {
				System.out.println("doing query");
				search = "%" + request.getParameter("q").replace(" " , "%") + "%";
				Pageable pageable = PageRequest.of(page, size);
				toreturn = service.getRepo().apiquery(search,pageable);
			}
			else {
				toreturn = service.getRepo().findAll(PageRequest.of(page, size));
			}
			
			model.addAttribute("pageTitle","Module Listing");
			model.addAttribute("modules", toreturn);
			return "module/list.html";
		}
		
		@GetMapping("/update")
		public String update(Model model) {
			System.out.println("Updating modules list");
			service.updatelisting();
			return "redirect:/admin/modules";
		}
		
		@PostMapping("/export/{id}")
		public String export(@PathVariable Long id, Model model) {
			Module module = service.getRepo().getOne(id);
			service.exportModule(module.getName());
			return "redirect:/admin/modules";
		}
		
		@PostMapping("/import/{id}")
		public String importModule(@PathVariable Long id, Model model) {
			Module module = service.getRepo().getOne(id);
			service.importModule(module.getName());
			return "redirect:/admin/modules";
		}
		
		@GetMapping("/copy/{id}")
		public String copy(@PathVariable Long id, Model model) {
			Module module = service.getRepo().getOne(id);			
			model.addAttribute("module",module);
			return "module/copy.html";
		}
		
		@PostMapping("/copy/{id}")
		public String docopy(@PathVariable Long id, Model model, HttpServletRequest request) {
			Module module = service.getRepo().getOne(id);
			service.copyModule(module.getName(),request.getParameter("new_module"));
			return "redirect:/admin/modules";
		}
		
		@GetMapping("/upload")
		public String upload(Model model) {
			return "module/upload.html";
		}
		
		@PostMapping("/upload")
		public String doupload(@RequestParam("upload_module") MultipartFile upload_module, Model model, HttpServletRequest request) {
			service.uploadModule(upload_module);
			service.updatelisting();
			return "redirect:/admin/modules";
		}
}
