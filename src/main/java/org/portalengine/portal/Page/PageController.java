package org.portalengine.portal.Page;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
@RequestMapping("/pages")
public class PageController {
	
		@Autowired
		private PageService service;
		
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
			model.addAttribute("pages", service.getRepo().findAll(PageRequest.of(page, size)));
			return "page/list.html";
		}
		
		@GetMapping(value= {"/create","/edit/{id}"})
		public String edit(@PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				Page curpage = service.getRepo().getOne(id);
				model.addAttribute("page", curpage);
			}
			else {
				model.addAttribute("page", new Page());
			}
			
			return "page/form.html";
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Page curpage = service.getRepo().getOne(id);
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		
		@GetMapping("/runpage/{id}")
		public String runpage(@PathVariable Long id, Model model) {
			Page curpage = service.getRepo().getOne(id);
			if(curpage!=null && curpage.getRunable()) {
			
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				binding.setVariable("service",service);
				String content = (String) shell.evaluate(curpage.getContent());
			
			}
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Page page,Model model) {
			service.getRepo().save(page);
			return "redirect:/pages";
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.getRepo().deleteById(id);
			return "redirect:/pages";
		}
}
