package org.portalengine.portal;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.web.multipart.MultipartFile;
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
	
	@GetMapping("/p/**/edit")
	public String editResponse(Model model) {
		String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/edit", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findByFullPath(pathuri);
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
	
	@PostMapping("/p/**/save")
	public String saveResponse(@RequestParam Map<String,String> postdata, @RequestParam("file") Optional<MultipartFile> file) {
		String pathuri = request.getRequestURI();		
		pathuri = pathuri.replaceAll("/p/", "portal/").replaceAll("/save", "");
		System.out.println("pathuri:" + pathuri);
		TreeNode pnode = treeService.getNodeRepo().findByFullPath(pathuri);
		if(pnode!=null) {
			//Map<String, String[]> postdata = request.getParameterMap();
			if(postdata.get("objectType")!=null) {
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
						treeService.getNodeRepo().save(newnode);
						return "redirect:/p/" + pnode.rootLessPath() + "/" + newnode.getSlug();
					}
					else {
						return "redirect:/p/" + pnode.rootLessPath();
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
						treeService.getNodeRepo().save(newnode);
					}
					return "redirect:/p/" + pnode.rootLessPath();
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
						return "redirect:/p/" + newnode.rootLessPath();
					}
					else {
						cnode.setName(postdata.get("name"));
						treeService.getNodeRepo().save(cnode);
						return "redirect:/p/" + cnode.rootLessPath();
					}
				}
			}
		}
		return "whatsave";
	}

}
