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
		String[] pathpart = request.getRequestURI().split("/");
		String pathuri = request.getRequestURI().replaceAll("/p/", "");
		TreeNode curnode = treeService.getNodeRepo().findByFullpath(pathuri);
		if(curnode!=null) {
			System.out.println("curnode:" + curnode.getName());
			if(curnode.getObjectType().equals("Page")) {
				Page curpage = pageService.getRepo().getOne(curnode.getObjectId());
				if(curpage==null) {
					System.out.println("Page not found");
				}
				else {
					model.addAttribute("page",curpage);
					return "page/display.html";
				}
			}
			else if(curnode.getObjectType().equals("File")) {
				FileLink curfile = fileService.getRepo().getOne(curnode.getObjectId());
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
			System.out.println("node not found");
		}
		return "what " + pathuri;
	}
	
	@GetMapping("/p/**/edit")
	public String editResponse() {
		return "whatedit";
	}
	
	@PostMapping("/p/**/save")
	public String saveResponse() {
		return "whatsave";
	}

}
