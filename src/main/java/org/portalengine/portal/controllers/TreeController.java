package org.portalengine.portal.controllers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.entities.DataSet;
import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Setting;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.Tree;
import org.portalengine.portal.entities.TreeNode;
import org.portalengine.portal.entities.TreeUser;
import org.portalengine.portal.services.FileLinkService;
import org.portalengine.portal.services.PageService;
import org.portalengine.portal.services.SettingService;
import org.portalengine.portal.services.TrackerService;
import org.portalengine.portal.services.TreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/admin/trees")
public class TreeController {
	
	@Autowired
	private TreeService service;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileLinkService;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private SettingService settingService;
	
	@Autowired
	public TreeController() {
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
		model.addAttribute("trees", service.getTreeRepo().findAll(PageRequest.of(page, size)));
		model.addAttribute("pageTitle","Tree Listing");
		return "tree/list.html";
	}
	
	@GetMapping(value={"/create","/edit/{id}"})
	public String form(Model model,@PathVariable(required=false) Long id) {
		if(id!=null) {
			Tree curtree = service.getTreeRepo().getById(id);
			model.addAttribute("pageTitle","Edit Tree - " + curtree.getName());
			model.addAttribute("tree", curtree);	
		}
		else {
			model.addAttribute("pageTitle","Create Tree");
			model.addAttribute("tree", new Tree());	
		}
		
		return "tree/form.html";
	}
	
	@GetMapping("/fixtree/{id}")
	public String fixtree(@PathVariable Long id, Model model) {
		Tree curtree = service.getTreeRepo().getById(id);
		service.fixTree(curtree);
		return "redirect:/admin/trees/display/" + String.valueOf(id);
	}
	
	@GetMapping(value={"/display/{id}","/display/{id}/{node_id}"})
	public String display(@PathVariable Long id, Model model, @PathVariable(required=false) Long node_id) {
		Tree curtree = service.getTreeRepo().getById(id);
		model.addAttribute("pageTitle","Tree - " + curtree.getName());
		model.addAttribute("tree", curtree);
		TreeNode curnode = service.getRoot(curtree);
		if(node_id!=null) {
			curnode = service.getNodeRepo().getById(node_id);
		}
		model.addAttribute("curnode",curnode);
		return "tree/display.html";
	}
	
	@PostMapping("/save")
	public String save(@Valid Tree tree,Model model) {
		service.saveTree(tree);
		return "redirect:/admin/trees";
	}
	
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, Model model) {
		service.getTreeRepo().deleteById(id);
		return "redirect:/admin/trees";
	}
	
	@PostMapping("/nodes/save")
	public String saveNode(Model model, HttpServletRequest request) {
		Map<String, String[]> postdata = request.getParameterMap();
		TreeNode parentnode = service.getNodeRepo().getById(Long.parseLong(postdata.get("parent_id")[0]));
		TreeNode newnode = service.addNode(parentnode, postdata.get("name")[0], "last");
		return "redirect:/admin/trees/display/" + parentnode.getTree().getId().toString() + "/" + newnode.getId();
	}
	
	@GetMapping("/nodes/{id}/create")
	public String createNode(@PathVariable Long id, Model model) {
		TreeNode parentnode = service.getNodeRepo().getById(id);
		TreeNode newnode = new TreeNode();
		model.addAttribute("pageTitle","Create Node - " + parentnode.getTree().getName());
		newnode.setParent(parentnode);
		newnode.setTree(parentnode.getTree());
		model.addAttribute("newnode",newnode);
		return "tree/node/form.html";
	}
	
	@GetMapping("/nodes/{id}/edit")
	public String editNode(@PathVariable Long id, Model model) {
		TreeNode curnode = service.getNodeRepo().getById(id);
		model.addAttribute("pageTitle","Edit Node - " + curnode.getName());
		model.addAttribute("curnode",curnode);
		ArrayList<String> objectTypes = new ArrayList<String>(Arrays.asList("","Folder","Page","File","Tracker","Record","TreeNode"));
		ArrayList<String> nodeStatuses = new ArrayList<String>(Arrays.asList("Private","Published"));
		
		Setting customTypes = settingService.getRepo().findByModuleAndName("portal", "TrackerType");
		if(customTypes!=null) {
			System.out.println("found settings");
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode trackerType = mapper.readTree(customTypes.getTextValue());
				if(trackerType.isArray()) {
					for(final JsonNode jnode : trackerType) {
						objectTypes.add(jnode.get("name").asText());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		model.addAttribute("nodeStatuses",nodeStatuses);
		model.addAttribute("objectTypes",objectTypes);
		return "tree/node/edit.html";
	}
	
	@PostMapping("/nodes/{id}/update")
	public String updateNode(@RequestParam Map<String,String> postdata, @PathVariable Long id, HttpServletRequest request,Model model) {
		TreeNode curnode = service.getNodeRepo().getById(id);
		curnode.setName(postdata.get("name"));
		curnode.setObjectType(postdata.get("objectType"));
		if(postdata.get("objectId")!="") {
			Long objectId = Long.parseLong(postdata.get("objectId"));
			if(postdata.get("objectType").equals("Tracker")) {
				Tracker curtracker = trackerService.getRepo().getById(objectId);
				if(curtracker!=null) {	
					curnode.setObjectId(curtracker.getId());						
				}
			}
			else {
				curnode.setObjectId(objectId);
			}
		}
		if(postdata.get("recordId")!="") {
			curnode.setRecordId(Long.parseLong(postdata.get("recordId")));
		}
		curnode.setData(postdata.get("data"));
		curnode.setStatus(postdata.get("status"));
		service.getNodeRepo().save(curnode);
		for(TreeUser tn:curnode.getUsers()) {
			if(postdata.get("delrole_" + tn.getId().toString())!=null) {
				if(postdata.get("delrole_" + tn.getId().toString()).equals("delete")) {
					service.getUserRepo().delete(tn);
				}	
			}
		}
		return "redirect:/admin/trees/display/" + curnode.getTree().getId().toString() + "/" + curnode.getId().toString();
	}
	
	@PostMapping("/objectSearch")
	public String objectSearch(@RequestParam Map<String,String> postdata, Model model) {
		String searchType = postdata.get("searchType").toLowerCase();
		String tosearch = postdata.get("q");
		tosearch = "%" + tosearch.replaceAll(" ", "%") + "%";
		System.out.println("Searching:" + tosearch);
		if(searchType.equals("page")) {
			List<PortalPage> pages = pageService.getRepo().findAllByQ(tosearch);
			model.addAttribute("pages",pages);
		}
		else if(searchType.equals("treenode")) {
			String source = "";
			if(postdata.get("source")!=null) {
				source = postdata.get("source");
			}
			List<TreeNode> nodes;
			if(source.equals("editpage")) {
				nodes = service.getNodeRepo().findAllPublishedByQ(tosearch);
			}
			else {
				nodes = service.getNodeRepo().findAllByQ(tosearch);
			}
			model.addAttribute("nodes",nodes);
		}
		else if(searchType.equals("file")) {
			List<FileLink> files = fileLinkService.getRepo().findAllByQ(tosearch);
			model.addAttribute("files",files);
		}
		else if(searchType.equals("tracker")) {
			List<Tracker> trackers = trackerService.getRepo().findAllByQ(tosearch);
			model.addAttribute("trackers",trackers);
		}
		else if(searchType.equals("folder")) {
			List<TreeNode> nodes = service.getNodeRepo().findAllByQ(tosearch);
			model.addAttribute("nodes",nodes);
		}
		else {				
			Setting customTypes = settingService.getRepo().findByModuleAndName("portal", "TrackerType");
			if(customTypes!=null) {
				System.out.println("found settings");
				ObjectMapper mapper = new ObjectMapper();
				try {
					JsonNode trackerType = mapper.readTree(customTypes.getTextValue());
					if(trackerType.isArray()) {
						for(final JsonNode jnode : trackerType) {
							System.out.println("testing json:" + jnode.toString());
							String curname = jnode.get("name").asText().toLowerCase();
							System.out.println("comparing:" + curname + " with:" + searchType);
							if(searchType.equals(curname)) {
								JsonNode search = ((ObjectNode)jnode.get("search")).put("q",tosearch);									
								System.out.println("found type:" + curname);
								model.addAttribute("object",jnode);
								Tracker tracker = trackerService.getRepo().getById((long) jnode.get("objectId").asInt());
								DataSet dataset = trackerService.dataset(tracker,search,false);
								System.out.println("datas:" + dataset.getDataRows().toString());
								model.addAttribute("items",dataset.getDataRows());
								model.addAttribute("title",jnode.get("title").asText());
								if(jnode.get("detail")!=null){
									model.addAttribute("detail",jnode.get("detail").asText());
								}
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			searchType = "trackertype";
		}
		return "tree/node/object_search/" + searchType + ".html";
	}
		
}
