package org.portalengine.portal.User.Role;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users/roles")
public class UserRoleController {

	@Autowired
	private UserService service;

	@Autowired
	public UserRoleController() {
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
		model.addAttribute("user_roles", service.getRoleRepo().findAll(PageRequest.of(page, size)));
		return "user/role/list.html";
	}

	@GetMapping("/create")
	public String create(Model model) {
		UserRole user_role = new UserRole();
		model.addAttribute("user_role", user_role);
		return "user/role/form.html";
	}
	
	@GetMapping("/{role_id}")
	public String display(@PathVariable Long role_id, Model model) {
		UserRole user_role = service.getRoleRepo().getOne(role_id);
		model.addAttribute("user_role", user_role);
		return "user/role/display.html";
	}

	@GetMapping("/edit/{role_id}")
	public String create_status(@PathVariable Long role_id, Model model) {
		UserRole user_role = service.getRoleRepo().getOne(role_id);
		model.addAttribute("user_role", user_role);
		return "user/role/form.html";
	}

	@PostMapping("/delete/{role_id}")
	public String delete_status(@PathVariable Long role_id, Model model) {
		UserRole user_role = service.getRoleRepo().getOne(role_id);
		if(user_role!=null) {
			service.getRoleRepo().deleteById(role_id);
		}
		return "redirect:/users/roles";
	}

	@PostMapping("/save")
	public String save_role(@Valid UserRole user_role, Model model) {
		service.getRoleRepo().save(user_role);
		return "redirect:/users/roles";
	}
}
