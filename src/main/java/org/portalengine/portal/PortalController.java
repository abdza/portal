package org.portalengine.portal;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
import org.portalengine.portal.Tree.TreeNode;
import org.portalengine.portal.Tree.TreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.tags.Param;

@Controller
@RequestMapping("/")
public class PortalController {
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private TreeService treeService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	public PortalController() {
	}
	
	@GetMapping("/p/**")
	public Object siteResponse(Model model) {
		String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/p/", "portal/");
		
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findByFullPath(pathuri);
		if(pnode!=null) {
			model.addAttribute("pnode",pnode);
			System.out.println("pnode:" + pnode.getName() + " fullpath:" + pnode.getFullPath());
			if(pnode.getObjectType()!=null) {
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
			}
			else {
				return "tree/node/listing.html";
			}
		}
		else {
			System.out.println("node not found");
		}
		return "what " + pathuri;
	}
	
	@GetMapping("/p/**/create")
	public String createResponse(@RequestParam String objectType, Model model) {
		String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/create", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findByFullPath(pathuri);
		if(pnode!=null) {
			model.addAttribute("pnode",pnode);
			System.out.println("pnode:" + pnode.getName());
			
			if(objectType.equals("page")) {
				Page page = new Page();
				model.addAttribute("page",page);
				return "tree/node/form/page.html";
			}
		}
		return "whatcreate " + objectType ;
	}
	
	@GetMapping("/p/**/edit")
	public String editResponse() {
		return "whatedit";
	}
	
	@PostMapping("/p/**/save")
	public String saveResponse() {
		String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/save", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findByFullPath(pathuri);
		if(pnode!=null) {
			Map<String, String[]> postdata = request.getParameterMap();
			if(postdata.get("objectType")!=null) {
				if(postdata.get("objectType")[0].equals("page")) {
					Page newpage = new Page();
					newpage.setTitle(postdata.get("title")[0]);
					newpage.setModule("content");
					newpage.setContent(postdata.get("content")[0]);
					newpage.setSlug(treeService.slugify(postdata.get("title")[0]));
					pageService.getRepo().save(newpage);
					TreeNode newnode = treeService.addNode(pnode, postdata.get("title")[0], "last");
					newnode.setObjectType("Page");
					newnode.setObjectId(newpage.getId());
					treeService.getNodeRepo().save(newnode);
					System.out.println("Saving page");
					return "redirect:/p/" + pnode.rootLessPath() + "/" + newnode.getSlug();
				}
			}
		}
		return "whatsave";
	}

}
