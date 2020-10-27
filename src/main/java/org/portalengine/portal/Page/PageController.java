package org.portalengine.portal.Page;


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
@RequestMapping("/admin/pages")
public class PageController {
	
		@Autowired
		private PageService service;
		
		@Autowired
		private TrackerService trackerService;
		
		@Autowired
		private TreeService treeService;
		
		@Autowired
		private UserService userService;
		
		@Autowired
		private FileLinkService fileService;
		
		@Autowired
		public PageController() {
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
			model.addAttribute("pageTitle","Page Listing");
			model.addAttribute("pages", service.getRepo().findAll(PageRequest.of(page, size)));
			return "page/list.html";
		}
		
		@GetMapping(value= {"/create","/edit/{id}"})
		public String edit(@PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				Page curpage = service.getRepo().getOne(id);
				model.addAttribute("pageTitle","Edit Page - " + curpage.getTitle());
				model.addAttribute("page", curpage);
			}
			else {
				model.addAttribute("pageTitle","Create Page");
				model.addAttribute("page", new Page());
			}
			
			return "page/form.html";
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Page curpage = service.getRepo().getOne(id);
			model.addAttribute("pageTitle",curpage.getTitle());
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		
		@GetMapping("/runpage/{id}")
		public String runpage(@PathVariable Long id, Model model) {
			Page curpage = service.getRepo().getOne(id);
			if(curpage!=null && curpage.getRunable()) {
			
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				binding.setVariable("pageService",service);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				try {
					String content = (String) shell.evaluate(curpage.getContent());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
			
			}
			model.addAttribute("pageTitle","Running " + curpage.getTitle());
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Page page,Model model) {
			service.getRepo().save(page);
			return "redirect:/admin/pages/edit/" + page.getId().toString();
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.getRepo().deleteById(id);
			return "redirect:/admin/pages";
		}
}
