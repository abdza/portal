package org.portalengine.portal.User;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tree.Tree;
import org.portalengine.portal.Tree.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
	
	@Autowired
	private UserService service;
	
	/* Read application.properties with the following function:
	 * String keyValue = env.getProperty(key);
	 */
	@Autowired
	private Environment env;
		
	@GetMapping("/update_password")
	public String updatePassword(UpdatePasswordForm updatePasswordForm, Model model) {
		model.addAttribute("pageTitle","User Profile");
		model.addAttribute("user",service.currentUser());
		return "user/update_password.html";
	}
	
	@PostMapping("/update_password")
	public String postUpdatePassword(@Valid UpdatePasswordForm updatePasswordForm, final BindingResult bindingResult, Model model) {
		
		model.addAttribute("pageTitle","User Profile");
		model.addAttribute("user",service.currentUser());		
		
		if(updatePasswordForm.getPassword().length()<8) {
			FieldError error = new FieldError(bindingResult.getObjectName(), "password", "Password need to be more than 8 characters");
			bindingResult.addError(error);
		}
		
		if(!updatePasswordForm.getPassword().equals(updatePasswordForm.getRepeatPassword())) {
			FieldError error = new FieldError(bindingResult.getObjectName(), "repeatPassword", "Repeat password");
			bindingResult.addError(error);
		}
		
		if (bindingResult.hasErrors()) {			
			return "user/update_password.html";
		}
		
		User curuser = service.currentUser();
		curuser.setPassword(service.getPasswordEncoder().encode(updatePasswordForm.getPassword()));
		service.getRepo().save(curuser);
		
		return "redirect:/profile";
	}
	
	@GetMapping("/profile")
	public String profile(Model model) {
		if(service.currentUser()!=null) {
			model.addAttribute("pageTitle","User Profile");
			model.addAttribute("user",service.currentUser());
			return "user/profile.html";
		}
		else {
			return "redirect:/";
		}
	}
	
	@GetMapping("/register")
	public String registerPage(Model model) {
		String enable_register = env.getProperty("jpf.enable_register");
		if(enable_register!=null && enable_register.toLowerCase().trim().equals("true")) {
		model.addAttribute("pageTitle","New User Registration");
			return "user/register.html";
		}
		else {
			return "redirect:/";
		}
	}
	
	@PostMapping("/register")
	public String register(@Valid RegistrationForm userreg, Model model) {
		String enable_register = env.getProperty("jpf.enable_register");
		if(enable_register!=null && enable_register.toLowerCase().trim().equals("true")) {
			service.getRepo().save(userreg.toUser(service.getPasswordEncoder(),service));
		}
		return "redirect:/";
	}
	
	@GetMapping("/admin/users")
	public String list(HttpServletRequest request, Model model) {			
		int page = 0;
		int size = 10;
		if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
			page = Integer.parseInt(request.getParameter("page"));
		}
		if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
			size = Integer.parseInt(request.getParameter("size"));
		}
		model.addAttribute("pageTitle","User Listing");
		
		String search = "";
		Page<User> toreturn = null;
		if(request.getParameter("q")!=null) {
			search = "%" + request.getParameter("q").replace(" " , "%") + "%";		
			Pageable pageable = PageRequest.of(page, size);
			toreturn = service.getRepo().apiquery(search,pageable);
		}
		else {
			toreturn = service.getRepo().findAll(PageRequest.of(page, size));
		}
		
		model.addAttribute("users", toreturn);
		
		return "user/list.html";
	}
	
	@GetMapping(value= {"/admin/users/create","/admin/users/edit/{id}"})
	public String form(@PathVariable(required=false) Long id, Model model) {
		if(id!=null) {
			User user = service.getRepo().getOne(id);
			model.addAttribute("pageTitle","Edit User - " + user.getName());
			model.addAttribute("user", user);
		}
		else {
			model.addAttribute("pageTitle","Add User");
			model.addAttribute("user",new User());
		}
		return "user/form.html";
	}
	
	@GetMapping("/admin/users/display/{id}")
	public String display(@PathVariable Long id, Model model) {
		User curuser = service.getRepo().getOne(id);
		model.addAttribute("pageTitle","User - " + curuser.getName());
		model.addAttribute("user", curuser);
		return "user/display.html";
	}
	
	@GetMapping("/admin/users/switch/{id}")
	public String switchuser(@PathVariable Long id, Model model) {
		User curuser = service.getRepo().getOne(id);
		
		model.addAttribute("pageTitle","User - " + curuser.getName());
		model.addAttribute("user", curuser);
		return "redirect:/admin/users";
	}
	
	@PostMapping("/admin/users/save")
	public String save(@Valid RegistrationForm userreg,Model model) {
		service.getRepo().save(userreg.toUser(service.getPasswordEncoder(),service));
		return "redirect:/admin/users";
	}
	
	@PostMapping("/admin/users/delete/{id}")
	public String delete(@PathVariable Long id, Model model) {
		service.getRepo().deleteById(id);
		return "redirect:/admin/users";
	}

}
