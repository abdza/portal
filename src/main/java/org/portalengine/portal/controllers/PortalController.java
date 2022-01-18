package org.portalengine.portal.controllers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.Tree;
import org.portalengine.portal.entities.TreeNode;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.services.FileLinkService;
import org.portalengine.portal.services.ModuleService;
import org.portalengine.portal.services.PageService;
import org.portalengine.portal.services.SettingService;
import org.portalengine.portal.services.TrackerService;
import org.portalengine.portal.services.TreeService;
import org.portalengine.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.tags.Param;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.var;

@Controller
@RequestMapping("/")
public class PortalController {
	
	@Value("${server.servlet.context-path}")
	private String contextPath;
	
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
	private ModuleService moduleService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private SettingService settingService;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
    private JavaMailSender javaMailSender;
	
	/* Read application.properties with the following function:
	 * String keyValue = env.getProperty(key);
	 */
	@Autowired
	private Environment env;
	
	@Autowired
	public PortalController() {
	}
	
	@GetMapping("/testemail")
	public String testemail(Model model) {
		
		SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("abdullah.zainul@gmail.com");

        msg.setSubject("Testing from Spring Boot");
        msg.setText("Hello World \n Spring Boot Email");

        javaMailSender.send(msg);
        
		return "redirect:/";
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageTitle","Login");
		return "user/login";
	}
	
	@GetMapping("/")
	public String home(Model model) {
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug("portal", "home");
		model.addAttribute("pageTitle",settingService.StringSetting("home_title", "Home"));
		if(curpage!=null) {			
			model.addAttribute("page", curpage);
			model.addAttribute("content", curpage.getContent());
			return "page/plain.html";
		}
		else {			
			return "page/home";
		}
	}
	
	@GetMapping("/search")
	public String search(Model model) {
		model.addAttribute("pageTitle","Search");
		return "page/search";
	}
	
	@GetMapping("/setup")
	public String setupSite(Model model) {
		var o = userService.getRepo().findByUsername("admin");
		User admin = o.orElse(null);
		model.addAttribute("admin",admin);
		return "page/setup.html";
	}
	
	@PostMapping("/setup")
	public String doSetupSite(Model model, HttpServletRequest request) {
		var o = userService.getRepo().findByUsername("admin");
		User admin = o.orElse(null);
		
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
		model.addAttribute("admin",admin);
		String setup_modules = env.getProperty("setup_modules");		
		if(setup_modules!=null) {
			String[] modules = setup_modules.split(",");
			for(String rawmodule : modules ) {
				String module = rawmodule.trim();
				moduleService.importModule(module);				
				List<Tracker> trackers = trackerService.getRepo().findAllByModule(module);
				for(Tracker tracker : trackers) {					
					trackerService.updateDb(tracker);
				}	
				PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, "post_module_import");
				if(curpage!=null) {
					if(curpage.getRunable()) {
						Binding binding = new Binding();		
						GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
						Map<String, String[]> postdata = request.getParameterMap();
						binding.setVariable("pageService",pageService);
						binding.setVariable("postdata", postdata);
						binding.setVariable("request", request);
						binding.setVariable("trackerService",trackerService);
						binding.setVariable("treeService",treeService);
						binding.setVariable("userService",userService);
						binding.setVariable("fileService",fileService);
						binding.setVariable("settingService", settingService);
						binding.setVariable("javaMailSender", javaMailSender);
						binding.setVariable("passwordEncoder", passwordEncoder);
						binding.setVariable("namedjdbctemplate", namedjdbctemplate);
						binding.setVariable("env", env);				
						Object content = null;
						try {
							content = shell.evaluate(curpage.getContent());
						}
						catch(Exception e) {
							System.out.println("Error in page:" + e.toString());
						}
					}
				}
			}
		}
		String post_setup_page = env.getProperty("post_setup_page");
		if(post_setup_page!=null) {
			String[] psetup = post_setup_page.split(":");
			String mdl = "portal";
			String slg = "";
			if(psetup.length==2) {
				mdl = psetup[0].trim();
				slg = psetup[1].trim();
			}
			else if(psetup.length==1){
				slg = psetup[0].trim();
			}
			else {
				slg = post_setup_page.trim();
			}			
			PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(mdl, slg);
			if(curpage!=null) {			
				return "redirect:/view/" + mdl + "/" + slg;
			}
		}
		
		return "redirect:/";
	}
	
	@RequestMapping(path={"/img/{slug}","/img/{module}/{slug}","/img/{module}/{slug}/{arg1}","/img/{module}/{slug}/{arg1}/{arg2}","/img/{module}/{slug}/{arg1}/{arg2}/{arg3}","/img/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}","/img/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"}, produces = MediaType.IMAGE_PNG_VALUE, method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("pagePermission(#module,#slug)")
	@ResponseBody
	public Object pngPage(@PathVariable(required=false) String module, @PathVariable String slug, Model model,HttpServletRequest request,@PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);				
		if(curpage!=null) {
			if(curpage.getRunable()) {				
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);
				binding.setVariable("request", request);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				binding.setVariable("settingService", settingService);
				binding.setVariable("javaMailSender", javaMailSender);
				binding.setVariable("passwordEncoder", passwordEncoder);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				binding.setVariable("arg1", arg1);
				binding.setVariable("arg2", arg2);
				binding.setVariable("arg3", arg3);
				binding.setVariable("arg4", arg4);
				binding.setVariable("arg5", arg5);
				Object content = null;
				try {
					content = shell.evaluate(curpage.getContent());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
				return content;				
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	@RequestMapping(path={"/excel/{slug}","/excel/{module}/{slug}","/excel/{module}/{slug}/{arg1}","/excel/{module}/{slug}/{arg1}/{arg2}","/excel/{module}/{slug}/{arg1}/{arg2}/{arg3}","/excel/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}","/excel/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE , method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("pagePermission(#module,#slug)")
	@ResponseBody
	public ResponseEntity<StreamingResponseBody> xlsxPage(@PathVariable(required=false) String module, @PathVariable String slug, Model model,HttpServletRequest request,@PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);				
		if(curpage!=null) {
			if(curpage.getRunable()) {		
				SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
				Sheet sheet = wb.createSheet();
				String exceltitle = curpage.getTitle().toLowerCase().replace(" ", "_") + ".xlsx";
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				binding.setVariable("wb",wb);
				binding.setVariable("sheet", sheet);
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);
				binding.setVariable("request", request);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				binding.setVariable("settingService", settingService);
				binding.setVariable("javaMailSender", javaMailSender);
				binding.setVariable("passwordEncoder", passwordEncoder);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				binding.setVariable("arg1", arg1);
				binding.setVariable("arg2", arg2);
				binding.setVariable("arg3", arg3);
				binding.setVariable("arg4", arg4);
				binding.setVariable("arg5", arg5);
				Object content = null;
				try {
					content = shell.evaluate(curpage.getContent());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
				return ResponseEntity
					    .ok()
					    .contentType(MediaType.APPLICATION_OCTET_STREAM)
					    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"" + exceltitle + "\"")
					    .body(wb::write);			
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	@RequestMapping(path={"/api/{slug}","/api/{module}/{slug}","/api/{module}/{slug}/{arg1}","/api/{module}/{slug}/{arg1}/{arg2}","/api/{module}/{slug}/{arg1}/{arg2}/{arg3}","/api/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}","/api/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"}, produces = MediaType.APPLICATION_JSON_VALUE, method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("pagePermission(#module,#slug)")
	@ResponseBody
	public Object apiPage(HttpEntity<String> httpEntity, @PathVariable(required=false)  String module, @PathVariable String slug, Model model,HttpServletRequest request,@PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);				
		if(curpage!=null) {
			if(curpage.getRunable() && curpage.getPage_type().toLowerCase().equals("api")) {
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				String json = httpEntity.getBody();
				ObjectMapper mapper = new ObjectMapper();
			    JsonNode qjson = null;
			    if(json!=null) {
					try {				
						qjson = mapper.readTree(json.replace('`', '"'));				
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);
				binding.setVariable("json", qjson);
				binding.setVariable("request", request);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				binding.setVariable("settingService", settingService);
				binding.setVariable("javaMailSender", javaMailSender);
				binding.setVariable("passwordEncoder", passwordEncoder);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				binding.setVariable("arg1", arg1);
				binding.setVariable("arg2", arg2);
				binding.setVariable("arg3", arg3);
				binding.setVariable("arg4", arg4);
				binding.setVariable("arg5", arg5);
				Object content = null;
				try {
					content = shell.evaluate(curpage.getContent());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
				return content;				
			}
			else {
				System.out.println("api page not runable or not api type");
				return null;
			}
		}
		else {
			System.out.println("api page not found");
			return null;
		}
	}
	
	@RequestMapping(path={"/json/{slug}","/json/{module}/{slug}","/json/{module}/{slug}/{arg1}","/json/{module}/{slug}/{arg1}/{arg2}","/json/{module}/{slug}/{arg1}/{arg2}/{arg3}","/json/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}","/json/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"}, produces = MediaType.APPLICATION_JSON_VALUE, method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("pagePermission(#module,#slug)")
	@ResponseBody
	public Object jsonPage(@PathVariable(required=false)  String module, @PathVariable String slug, Model model,HttpServletRequest request,@PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);				
		if(curpage!=null) {
			if(curpage.getRunable() && curpage.getPage_type().toLowerCase().equals("json")) {
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);
				binding.setVariable("request", request);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				binding.setVariable("settingService", settingService);
				binding.setVariable("javaMailSender", javaMailSender);
				binding.setVariable("passwordEncoder", passwordEncoder);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				binding.setVariable("arg1", arg1);
				binding.setVariable("arg2", arg2);
				binding.setVariable("arg3", arg3);
				binding.setVariable("arg4", arg4);
				binding.setVariable("arg5", arg5);
				Object content = null;
				try {
					content = shell.evaluate(curpage.getContent());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
				return content;				
			}
			else {
				System.out.println("Json page not runable or not json type");
				return null;
			}
		}
		else {
			System.out.println("Json page not found");
			return null;
		}
	}
	
	@RequestMapping(path={"/data/{slug}","/data/{module}/{slug}","/data/{module}/{slug}/{arg1}","/data/{module}/{slug}/{arg1}/{arg2}","/data/{module}/{slug}/{arg1}/{arg2}/{arg3}","/data/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}","/data/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"}, produces = MediaType.APPLICATION_JSON_VALUE, method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("pagePermission(#module,#slug)")
	@ResponseBody
	public Object dataPage(@PathVariable(required=false)  String module, @PathVariable String slug, Model model,HttpServletRequest request,@PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);				
		if(curpage!=null) {
			if(curpage.getPageData()!=null && curpage.getPageData().length()>0) {
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);
				binding.setVariable("request", request);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				binding.setVariable("settingService", settingService);
				binding.setVariable("javaMailSender", javaMailSender);
				binding.setVariable("passwordEncoder", passwordEncoder);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				binding.setVariable("arg1", arg1);
				binding.setVariable("arg2", arg2);
				binding.setVariable("arg3", arg3);
				binding.setVariable("arg4", arg4);
				binding.setVariable("arg5", arg5);
				Object content = null;
				try {
					content = shell.evaluate(curpage.getPageData());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
				return content;				
			}
			else {
				System.out.println("page data does not exists");
				return null;
			}
		}
		else {
			System.out.println("data page not found");
			return null;
		}
	}
	
	@RequestMapping(path={"/view/{slug}","/view/{module}/{slug}","/view/{module}/{slug}/{arg1}","/view/{module}/{slug}/{arg1}/{arg2}","/view/{module}/{slug}/{arg1}/{arg2}/{arg3}","/view/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}","/view/{module}/{slug}/{arg1}/{arg2}/{arg3}/{arg4}/{arg5}"},method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("pagePermission(#module,#slug)")
	public String viewPage(@PathVariable(required=false) String module, @PathVariable String slug, Model model,HttpServletRequest request,@PathVariable(required=false) String arg1,@PathVariable(required=false) String arg2,@PathVariable(required=false) String arg3,@PathVariable(required=false) String arg4,@PathVariable(required=false) String arg5) {
		
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = pageService.getRepo().findOneByModuleAndSlug(module, slug);
				
		if(curpage!=null) {
			if(curpage.getPublished()!=null && curpage.getPublished()==true) {
				if(curpage.getRunable()) {
					if(curpage.getPage_type().toLowerCase().equals("json")) {
						return "redirect:/json/" + module + "/" + slug;
					} 
					else if(curpage.getPage_type().toLowerCase().equals("api")) {
						return "redirect:/api/" + module + "/" + slug;
					}
					Binding binding = new Binding();		
					GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
					Map<String, String[]> postdata = request.getParameterMap();
					binding.setVariable("pageService",pageService);
					binding.setVariable("postdata", postdata);				
					binding.setVariable("request", request);
					binding.setVariable("trackerService",trackerService);
					binding.setVariable("treeService",treeService);
					binding.setVariable("userService",userService);
					binding.setVariable("fileService",fileService);
					binding.setVariable("settingService", settingService);	
					binding.setVariable("javaMailSender", javaMailSender);
					binding.setVariable("passwordEncoder", passwordEncoder);
					binding.setVariable("namedjdbctemplate", namedjdbctemplate);
					binding.setVariable("env", env);
					binding.setVariable("arg1", arg1);
					binding.setVariable("arg2", arg2);
					binding.setVariable("arg3", arg3);
					binding.setVariable("arg4", arg4);
					binding.setVariable("arg5", arg5);
					String content = null;
					try {
						content = (String) shell.evaluate(curpage.getContent());					
					}
					catch(Exception e) {
						System.out.println("Error in page:" + e.toString());
					}
					if(curpage.getPage_type().toLowerCase().equals("template")) {
						model.addAttribute("pageTitle","Running " + curpage.getTitle());
						model.addAttribute("page", curpage);
						model.addAttribute("content",content);
						model.addAttribute("env", env);
						model.addAttribute("arg1",arg1);
						model.addAttribute("arg2",arg2);
						model.addAttribute("arg3",arg3);
						model.addAttribute("arg4",arg4);
						model.addAttribute("arg5",arg5);
						return "page/plain.html";	
					}				
					else {
						// if wish to redirect then page should return a string beginning with "redirect:/" and target
						return content;
					}
				}
				else {  // page is not runable
					Object pdata = null;
					if(curpage.getPageData()!=null && curpage.getPageData().length()>0) {
						Binding binding = new Binding();		
						GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
						Map<String, String[]> postdata = request.getParameterMap();
						binding.setVariable("pageService",pageService);
						binding.setVariable("postdata", postdata);
						binding.setVariable("request", request);
						binding.setVariable("trackerService",trackerService);
						binding.setVariable("treeService",treeService);
						binding.setVariable("userService",userService);
						binding.setVariable("fileService",fileService);
						binding.setVariable("settingService", settingService);
						binding.setVariable("javaMailSender", javaMailSender);
						binding.setVariable("passwordEncoder", passwordEncoder);
						binding.setVariable("namedjdbctemplate", namedjdbctemplate);
						binding.setVariable("env", env);
						binding.setVariable("arg1", arg1);
						binding.setVariable("arg2", arg2);
						binding.setVariable("arg3", arg3);
						binding.setVariable("arg4", arg4);
						binding.setVariable("arg5", arg5);
						try {
							pdata = shell.evaluate(curpage.getPageData());
						}
						catch(Exception e) {
							System.out.println("Error in page:" + e.toString());
						}
					}
					model.addAttribute("pageTitle",curpage.getTitle());
					model.addAttribute("page", curpage);
					model.addAttribute("pdata", pdata);
					model.addAttribute("content", curpage.getContent());
					model.addAttribute("env", env);
					model.addAttribute("arg1",arg1);
					model.addAttribute("arg2",arg2);
					model.addAttribute("arg3",arg3);
					model.addAttribute("arg4",arg4);
					model.addAttribute("arg5",arg5);
					return "page/plain.html";
				}
			}
			else {
				return "error/403";
			}
		}
		else {
			return "error/404";
		}
	}	
	
	@GetMapping(path={"/download/{slug}","/download/{module}/{slug}"})	
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@PathVariable(required=false)  String module, @PathVariable String slug, Model model,HttpServletRequest request) {
		if(module==null) {
			module = "portal";
		}
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
