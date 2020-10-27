package org.portalengine.portal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
import org.portalengine.portal.Tracker.SystemController;
import org.portalengine.portal.Tree.Tree;
import org.portalengine.portal.Tree.TreeNode;
import org.portalengine.portal.Tree.TreeService;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.tags.Param;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Controller
@RequestMapping("/")
public class PortalController {
	
	@Value("${server.servlet.context-path}")
	private String contextPath;
	
	@Value("${rootnode:'portal'}")
	private String rootnode;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private TreeService treeService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	public PortalController() {
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageTitle","Login");
		return "user/login";
	}
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("pageTitle","Home");
		return "page/home";
	}
	
	@GetMapping("/search")
	public String search(Model model) {
		model.addAttribute("pageTitle","Search");
		return "page/search";
	}
	
	@GetMapping("/setup")
	public String setupSite(Model model) {
		User admin = userService.getRepo().findByUsername("admin");
		model.addAttribute("admin",admin);
		return "page/setup.html";
	}
	
	@PostMapping("/setup")
	public String doSetupSite(Model model) {
		User admin = userService.getRepo().findByUsername("admin");
		model.addAttribute("admin",admin);
		if(admin==null) {
			admin = new User("admin", "admin", "Admin", "admin@portal.com", passwordEncoder.encode("admin123"), true);
			userService.getRepo().save(admin);
		}
		else {
			if(!admin.getIsAdmin()) {
				admin.setIsAdmin(true);
				userService.getRepo().save(admin);
			}
		}
		return "page/setup.html";
	}	
	
	@GetMapping("/view/{module}/{slug}")
	public String viewPage(@PathVariable String module, @PathVariable String slug, Model model) {				
		Page curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);
				
		if(curpage!=null) {
			model.addAttribute("pageTitle",curpage.getTitle());
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		else {
			return "error/404";
		}
	}
	
	@GetMapping("/run/{module}/{slug}")
	public String runPage(@PathVariable String module, @PathVariable String slug, Model model) {
		Page curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);
		
		if(curpage!=null && curpage.getRunable()) {
			
			Binding binding = new Binding();		
			GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
			binding.setVariable("pageService",pageService);
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
		
			model.addAttribute("pageTitle","Running " + curpage.getTitle());
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		else {
			return "error/404";
		}
	}
	
	@GetMapping("/download/{module}/{slug}")
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@PathVariable String module, @PathVariable String slug, Model model) {
		FileLink curfile = fileService.getRepo().findOneByModuleAndSlug(module, slug);
		if(curfile!=null) {
			Resource resfile = fileService.getResource(curfile);
			if(resfile!=null) {
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + curfile.getName() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resfile);		
			}
			else {
				return ResponseEntity.notFound().build();
			}
		}
		else {
			return ResponseEntity.notFound().build();
		}
	}	
	
}
