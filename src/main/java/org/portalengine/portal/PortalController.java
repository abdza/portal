package org.portalengine.portal;


import java.util.HashMap;
import java.util.Map;

import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class PortalController {
	
	@Autowired
	private PageService pageservice;
	
	@Autowired
	private FileLinkService fileservice;
	
	@Autowired
	private TrackerService trackerservice;
	
	@Autowired
	public PortalController() {
	}
	
	@GetMapping("/{module}/view/{slug}")
	public String view(@PathVariable String module, @PathVariable String slug, Model model) {
		Page curpage = pageservice.getRepo().findOneByModuleAndSlug(module, slug);
		if(curpage!=null) {
			model.addAttribute("page", curpage);
			return "page/display.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/download/{slug}")
	@ResponseBody
	public ResponseEntity<Resource> download(@PathVariable String module, @PathVariable String slug, Model model) {
		FileLink curfile = fileservice.getRepo().findOneByModuleAndSlug(module, slug);
		Resource resfile = fileservice.getResource(curfile);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + curfile.getName()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resfile);
	}

}
