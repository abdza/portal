package org.portalengine.portal.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.entities.Setting;
import org.portalengine.portal.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/settings")
public class SettingController {
	
		@Autowired
		private SettingService service;
		
		@Autowired
		public SettingController() {
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
			model.addAttribute("pageTitle","Setting Listing");
			
			String search = "";
			Page<Setting> toreturn = null;
			if(request.getParameter("q")!=null||(request.getParameter("module")!=null && !request.getParameter("module").equals("All"))) {
				System.out.println("doing query");
				String module = request.getParameter("module");
				search = "%" + request.getParameter("q").replace(" " , "%") + "%";
				Pageable pageable = PageRequest.of(page, size);
				if(module.equals("All")) {
					toreturn = service.getRepo().apiquery(search,pageable);
				}
				else {
					toreturn = service.getRepo().apimodulequery(search, module, pageable);
				}
			}
			else {
				toreturn = service.getRepo().findAll(PageRequest.of(page, size));
			}
			
			model.addAttribute("settings", toreturn);
			
			return "setting/list.html";
		}
		
		@GetMapping(value= {"/create/{type}","/edit/{id}"})
		public String edit(@PathVariable(required=false) String type, @PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				Setting cursetting = service.getRepo().getById(id);
				model.addAttribute("pageTitle","Edit Setting - " + cursetting.getName());
				model.addAttribute("setting", cursetting);
			}
			else {
				if(type==null) {
					type = "string";
				}
				Setting cursetting = new Setting();
				cursetting.setType(type);
				if(type.equals("json")) {
					cursetting.setTextValue("[]");
				}
				model.addAttribute("pageTitle","Create Setting");
				model.addAttribute("setting",cursetting);
			}
			return "setting/form.html";
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.getRepo().deleteById(id);
			return "redirect:/admin/settings";
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Setting cursetting = service.getRepo().getById(id);
			model.addAttribute("pageTitle","Setting - " + cursetting.getName());
			model.addAttribute("setting", cursetting);
			return "setting/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Setting setting,Model model, HttpServletRequest request) {
			Map<String, String[]> postdata = request.getParameterMap();
			if(!postdata.containsKey("cancel")){
				service.getRepo().save(setting);
			}
			return "redirect:/admin/settings";
		}
}
