package org.portalengine.portal.Setting;

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
			model.addAttribute("settings", service.getRepo().findAll(PageRequest.of(page, size)));
			return "setting/list.html";
		}
		
		@GetMapping(value= {"/create/{type}","/edit/{id}"})
		public String edit(@PathVariable(required=false) String type, @PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				Setting cursetting = service.getRepo().getOne(id);
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
			Setting cursetting = service.getRepo().getOne(id);
			model.addAttribute("pageTitle","Setting - " + cursetting.getName());
			model.addAttribute("setting", cursetting);
			return "setting/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Setting setting,Model model) {
			service.getRepo().save(setting);
			return "redirect:/admin/settings";
		}
}
