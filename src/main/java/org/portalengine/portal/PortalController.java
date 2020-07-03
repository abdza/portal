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
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	public PortalController() {
	}
	
	/* @GetMapping("/p/{**}/t/display/{id}")
	public Object trackerResponse(Model model,@PathVariable Long id) {
		//Long id=(long) 1768;
		String pathuri = request.getRequestURI().substring(0, request.getRequestURI().indexOf("/t/display"));
		model.addAttribute("pathuri",pathuri);
		System.out.println("pathuri:" + pathuri);
		pathuri = pathuri.replaceFirst("/p/", "portal/");
		if(pathuri.equals("portal/")) {
			pathuri = "portal";
		}
		
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			Tracker curtracker = trackerService.getRepo().getOne(pnode.getObjectId());
			if(curtracker==null) {
				System.out.println("Tracker not found");
			}
			else {
				return trackerService.displayData(model, curtracker,id);
			}
		}
		return "Got error somewhere";
	} */
	
	@GetMapping("/setup")
	public String setupSite(Model model) {
		User admin = userService.getRepo().findByUsername("admin");
		model.addAttribute("admin",admin);
		Tree portaltree = treeService.getTreeRepo().findOneByModuleAndSlug("portal", "portal");
		model.addAttribute("portaltree",portaltree);
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
		Tree portaltree = treeService.getTreeRepo().findOneByModuleAndSlug("portal", "portal");
		if(portaltree==null) {
			portaltree = new Tree();
			portaltree.setModule("portal");
			portaltree.setSlug("portal");
			portaltree.setName("Portal");
			treeService.saveTree(portaltree);
		}
		return "page/setup.html";
	}
	
	@GetMapping("/p/**")
	public Object siteResponse(Model model) {
		String pathuri = request.getRequestURI().replace(contextPath, "");
		String ops = "";
		Integer cutoff = pathuri.indexOf("/t/");
		if(cutoff>0) {
			ops = pathuri.substring(cutoff);
			pathuri = pathuri.substring(0,cutoff);
		}
		pathuri = pathuri.replaceFirst("/p/", "portal/");
		if(pathuri.equals("portal/")) {
			pathuri = "portal";
		}
		
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			
			if (!(auth instanceof AnonymousAuthenticationToken)) {
			        // userDetails = auth.getPrincipal()
				UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				List<String> userRoles = treeService.userRoles((User)userDetails, pnode);
				List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
				List<GrantedAuthority> currentAuthorities = new ArrayList<>(auth.getAuthorities());
				for(final GrantedAuthority crole:currentAuthorities) {
					if(!crole.getAuthority().contains("ROLE_NODE_")) {
						updatedAuthorities.add(crole);
					}
				}
				if(userRoles.size()>0) {			
					for(final String crole:userRoles) {
						updatedAuthorities.add(new SimpleGrantedAuthority(crole)); //add your role here [e.g., new SimpleGrantedAuthority("ROLE_NEW_ROLE")]
					}
					
				}
				Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
				SecurityContextHolder.getContext().setAuthentication(newAuth);
			}
			
			model.addAttribute("pnode",pnode);
			model.addAttribute("breadcrumb",treeService.getPath(pnode));
			System.out.println("pnode:" + pnode.getName() + " fullpath:" + pnode.getFullPath());
			if(pnode.getObjectType()!=null) {
				System.out.println("object is not null -----" + String.valueOf(pnode.getObjectType()) + "------");
				if(pnode.getObjectType().equals("Page")) {
					Page curpage = pageService.getRepo().getOne(pnode.getObjectId());
					if(curpage==null) {
						System.out.println("Page not found");
					}
					else {
						model.addAttribute("page",curpage);
						
						return "page/display.html";
					}
				}
				else if(pnode.getObjectType().equals("File")) {
					FileLink curfile = fileService.getRepo().getOne(pnode.getObjectId());
					if(curfile==null) {
						System.out.println("File not found");
					}
					else {
						Resource resfile = fileService.getResource(curfile);
						return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + curfile.getName() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resfile);
					}
				}
				else if(pnode.getObjectType().equals("") || pnode.getObjectType().equals("Folder")) {
					return "tree/node/listing.html";
				}
				else if(pnode.getObjectType().equals("Tracker")) {
					Tracker curtracker = trackerService.getRepo().getOne(pnode.getObjectId());
					System.out.println("tracker:" + pnode.getObjectId().toString());
					if(curtracker==null) {
						System.out.println("Tracker not found");
					}
					else {
						Long id = (long) -1;
						String operation = "list";
						if(cutoff>0) {
							// System.out.println("ops:" + ops);
							String[] tokens = ops.split("/");
							// System.out.println("tokens:" + tokens[0]);  // supposed to be empty
							// System.out.println("tokens:" + tokens[1]);  // supposed to be t
							// System.out.println("tokens:" + tokens[2]);  // supposed to be the action
							operation = tokens[2];
							id = Long.valueOf(tokens[3]);
						}
						if(operation.equals("display")) {
							return trackerService.displayData(model, curtracker, id);
						}
						else if(operation.equals("edit")) {
							return trackerService.editData(model, curtracker, id);
						}
						else if(operation.equals("create")) {
							return trackerService.createData(model, curtracker);
						}
						else {
							return trackerService.displayList(model, curtracker);
						}
					}
				}
			}
			else {
				System.out.println("Null is the object");
				return "tree/node/listing.html";
			}
		}
		else {
			System.out.println("node not found");
		}
		return "what " + pathuri;
	}
	
	@GetMapping("/p/**/t/create")
	public String trackerCreateResponse(Model model) {
		
		String pathuri = request.getRequestURI().replace(contextPath, "");
		String ops = "";
		Integer cutoff = pathuri.indexOf("/t/");
		if(cutoff>0) {
			ops = pathuri.substring(cutoff);
			pathuri = pathuri.substring(0,cutoff);
		}
		pathuri = pathuri.replaceFirst("/p/", "portal/");
		if(pathuri.equals("portal/")) {
			pathuri = "portal";
		}
		
		// String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/create", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			Tracker curtracker = trackerService.getRepo().getOne(pnode.getObjectId());
			model.addAttribute("pnode",pnode);
			return trackerService.createData(model, curtracker);
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/p/**/create")
	public String createResponse(@RequestParam String objectType, Model model) {
		
		String pathuri = request.getRequestURI().replace(contextPath, "");
		String ops = "";
		Integer cutoff = pathuri.indexOf("/t/");
		if(cutoff>0) {
			ops = pathuri.substring(cutoff);
			pathuri = pathuri.substring(0,cutoff);
		}
		pathuri = pathuri.replaceFirst("/p/", "portal/");
		if(pathuri.equals("portal/")) {
			pathuri = "portal";
		}
		
		// String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/create", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			model.addAttribute("pnode",pnode);
			System.out.println("pnode:" + pnode.getName());
			
			if(objectType.equals("Page")) {
				Page page = new Page();
				model.addAttribute("page",page);
				return "tree/node/form/page.html";
			}
			else if(objectType.equals("File")) {
				FileLink fileLink = new FileLink();
				model.addAttribute("fileLink",fileLink);
				return "tree/node/form/filelink.html";
			}
			else if(objectType.equals("Folder")) {
				TreeNode cnode = new TreeNode();
				model.addAttribute("cnode",cnode);
				return "tree/node/form/folder.html";
			}
		}
		return "whatcreate " + objectType ;
	}
	
	@GetMapping("/p/**/delete")
	public String deleteResponse(Model model) {
		String pathuri = request.getRequestURI().replace(contextPath, "");		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/delete", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			model.addAttribute("pnode",pnode);
			System.out.println("pnode:" + pnode.getName());
			boolean gotobject = false;
			if(pnode.getObjectType()!=null && pnode.getObjectType()!="") {
				if(pnode.getObjectType().equals("Page")) {
					Page page = pageService.getRepo().getOne(pnode.getObjectId());
					model.addAttribute("page",page);
					gotobject = true;
				}
				else if(pnode.getObjectType().equals("File")) {
					FileLink fileLink = fileService.getRepo().getOne(pnode.getObjectId());
					model.addAttribute("fileLink",fileLink);
					gotobject = true;
				}
			}
			if(!gotobject) {
				model.addAttribute("folder",pnode);
			}
		}
		return "tree/node/form/delete.html";
	}
	
	@PostMapping("/p/**/delete")
	public String postDeleteResponse(Model model) {
		String pathuri = request.getRequestURI().replace(contextPath, "");		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/delete", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			model.addAttribute("pnode",pnode);
			System.out.println("pnode:" + pnode.getName());
			boolean gotobject = false;
			if(pnode.getObjectType()!=null && pnode.getObjectType()!="") {
				if(pnode.getObjectType().equals("Page")) {
					Page page = pageService.getRepo().getOne(pnode.getObjectId());
					model.addAttribute("page",page);
					gotobject = true;
				}
				else if(pnode.getObjectType().equals("File")) {
					FileLink fileLink = fileService.getRepo().getOne(pnode.getObjectId());
					model.addAttribute("fileLink",fileLink);
					gotobject = true;
				}
			}
			if(!gotobject) {
				model.addAttribute("folder",pnode);
			}
			TreeNode parent = pnode.getParent();
			treeService.deleteNode(pnode);
			return "redirect:/p" + parent.rootLessPath();
		}
		return "tree/node/form/delete.html";
	}
	
	@GetMapping("/p/**/edit")
	public String editResponse(Model model) {
		String pathuri = request.getRequestURI().replace(contextPath, "");		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/edit", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			model.addAttribute("pnode",pnode);
			if(pnode.getObjectType()!=null && pnode.getObjectType()!="") {
				if(pnode.getObjectType().equals("Page")) {
					Page page = pageService.getRepo().getOne(pnode.getObjectId());
					model.addAttribute("page",page);
					return "tree/node/form/page.html";
				}
			}
			else {
				model.addAttribute("cnode",pnode);
				return "tree/node/form/folder.html";
			}
		}
		return "whatedit";
	}
	
	@PostMapping(value = "/p/**/saveimage")
	@ResponseBody
	public String saveImageResponse(@RequestParam Map<String,String> postdata, @RequestParam("file") MultipartFile[] file) {
		String pathuri = request.getRequestURI().replace(contextPath, "");		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/saveimage", "");
		System.out.println("save uri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			System.out.println("savedata:");
			System.out.println(postdata.toString());
			TreeNode pnodeparent = pnode.getParent();

			FileLink newfile = new FileLink();
			newfile.setName(file[0].getOriginalFilename());
			newfile.setModule("content");
			newfile.setSlug(treeService.slugify(file[0].getOriginalFilename()));
			if(file != null) {
				newfile.setType("user");
				newfile = fileService.SaveFile(file[0], newfile);
				fileService.getRepo().save(newfile);
			}
			TreeNode newnode = treeService.addNode(pnodeparent, file[0].getOriginalFilename(), "last");
			newnode.setObjectType("File");
			newnode.setObjectId(newfile.getId());
			treeService.getNodeRepo().save(newnode);
			return contextPath + "/p" + newnode.rootLessPath();
		}
		return "whatsave";
	}
	
	@PostMapping("/p/**/save")
	public String saveResponse(@RequestParam Map<String,String> postdata, @RequestParam("file") Optional<MultipartFile> file,Authentication authentication) {
		User curuser = null;
		if(authentication!=null) {
			curuser = (User)authentication.getPrincipal();
		}
		String pathuri = request.getRequestURI().replace(contextPath, "");		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/save", "");
		System.out.println("save uri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findFirstByFullPath(pathuri);
		if(pnode!=null) {
			System.out.println("savedata:");
			System.out.println(postdata.toString());
			if(pnode.getObjectType()!=null && pnode.getObjectType().equals("Tracker")) {
				Tracker curtracker = trackerService.getRepo().getOne(pnode.getObjectId());
				if(curtracker==null) {
					System.out.println("Tracker not found");
				}
				else {
					Long fid = trackerService.saveForm(curtracker, curuser);
					if(fid!=null) {
						/* if(postdata.get("id")!=null) {
							return "redirect:" + contextPath + "/" + pnode.portalPath() + "/t/display/" + postdata.get("id").toString();
						}
						else {			
							return "redirect:" + contextPath + "/" + pnode.portalPath();
						}	*/					
						
						return "redirect:/" + pnode.portalPath() + "/t/display/" + String.valueOf(fid);
					}
					else {
						return "redirect:/" + pnode.portalPath();
					}
				}
			}
			else if(postdata.get("objectType")!=null) {
				if(postdata.get("objectType").equals("Page")) {
					Page newpage = new Page();
					if(postdata.get("id")!=null && postdata.get("id")!="") {
						newpage = pageService.getRepo().getOne(Long.parseLong(postdata.get("id")));
					}					
					newpage.setTitle(postdata.get("title"));
					newpage.setModule("content");
					newpage.setContent(postdata.get("content"));
					newpage.setSlug(treeService.slugify(postdata.get("title")));
					pageService.getRepo().save(newpage);
					
					if(postdata.get("id")==null || postdata.get("id")=="") {
						TreeNode newnode = treeService.addNode(pnode, postdata.get("title"), "last");
						newnode.setObjectType("Page");
						newnode.setObjectId(newpage.getId());
						newnode.setStatus("Published");
						treeService.getNodeRepo().save(newnode);
						return "redirect:" + contextPath + "/p" + pnode.rootLessPath() + "/" + newnode.getSlug();
					}
					else {
						return "redirect:" + contextPath + "/p" + pnode.rootLessPath();
					}
				}
				else if(postdata.get("objectType").equals("File")) {
					FileLink newfile = new FileLink();
					boolean createNewNode = true;
					try {
						Long curid = Long.parseLong(postdata.get("id"));
						newfile = fileService.getRepo().getOne(curid);
						createNewNode = false;
					}
					catch(Exception e) {
						System.out.println("Id not found");
					}
					newfile.setName(postdata.get("name"));
					newfile.setModule("content");
					newfile.setSlug(treeService.slugify(postdata.get("name")));
					if(file != null) {
						newfile.setType("user");
						newfile = fileService.SaveFile(file.get(), newfile);
						fileService.getRepo().save(newfile);
					}
					if(createNewNode) {
						TreeNode newnode = treeService.addNode(pnode, postdata.get("name"), "last");
						newnode.setObjectType("File");
						newnode.setObjectId(newfile.getId());
						newnode.setStatus("Published");
						treeService.getNodeRepo().save(newnode);
					}
					return "redirect:" + contextPath + "/p" + pnode.rootLessPath();
				}
				else if(postdata.get("objectType").equals("Folder")) {
					TreeNode cnode = new TreeNode();
					boolean createNewNode = true;
					try {
						Long curid = Long.parseLong(postdata.get("id"));
						cnode = treeService.getNodeRepo().getOne(curid);
						createNewNode = false;
					}
					catch(Exception e) {
						System.out.println("Id not found");
					}
					if(createNewNode) {
						TreeNode newnode = treeService.addNode(pnode, postdata.get("name"), "last");
						newnode.setStatus("Published");
						treeService.getNodeRepo().save(newnode);
						return "redirect:" + contextPath + "/p" + newnode.rootLessPath();
					}
					else {
						cnode.setName(postdata.get("name"));
						treeService.getNodeRepo().save(cnode);
						return "redirect:" + contextPath + "/p" + cnode.rootLessPath();
					}
				}
			}
		}
		return "whatsave";
	}

}
