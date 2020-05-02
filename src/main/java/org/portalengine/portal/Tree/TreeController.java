package org.portalengine.portal.Tree;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/trees")
public class TreeController {
	
		@Autowired
		private TreeService service;
		
		@Autowired
		private PageService pageService;
		
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
			return "tree/list.html";
		}
		
		@GetMapping("/create")
		public String create(Model model) {
			model.addAttribute("tree", new Tree());
			return "tree/form.html";
		}
		
		@GetMapping("/edit/{id}")
		public String edit(@PathVariable Long id, Model model) {
			Tree curtree = service.getTreeRepo().getOne(id);
			model.addAttribute("tree", curtree);
			return "tree/form.html";
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Tree curtree = service.getTreeRepo().getOne(id);
			model.addAttribute("tree", curtree);
			TreeNode curnode = service.getRoot(curtree);
			model.addAttribute("curnode",curnode);
			return "tree/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Tree tree,Model model) {
			service.saveTree(tree);
			return "redirect:/trees";
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.getTreeRepo().deleteById(id);
			return "redirect:/trees";
		}
		
		@PostMapping("/nodes/save")
		public String saveNode(Model model, HttpServletRequest request) {
			Map<String, String[]> postdata = request.getParameterMap();
			TreeNode parentnode = service.getNodeRepo().getOne(Long.parseLong(postdata.get("parent_id")[0]));
			service.addNode(parentnode, postdata.get("name")[0], "last");
			return "redirect:/trees/display/" + parentnode.getTree().getId().toString();
		}
		
		@GetMapping("/nodes/{id}/create")
		public String createNode(@PathVariable Long id, Model model) {
			TreeNode parentnode = service.getNodeRepo().getOne(id);
			TreeNode newnode = new TreeNode();
			newnode.setParent(parentnode);
			newnode.setTree(parentnode.getTree());
			model.addAttribute("newnode",newnode);
			return "tree/node/form.html";
		}
		
		@GetMapping("/nodes/{id}/edit")
		public String editNode(@PathVariable Long id, Model model) {
			TreeNode curnode = service.getNodeRepo().getOne(id);
			model.addAttribute("curnode",curnode);
			String[] objectTypes = {"","Folder","Page","File","Tracker","Record"};
			model.addAttribute("objectTypes",objectTypes);
			return "tree/node/edit.html";
		}
		
		@PostMapping("/nodes/{id}/update")
		public String updateNode(@RequestParam Map<String,String> postdata, @PathVariable Long id, HttpServletRequest request,Model model) {
			TreeNode curnode = service.getNodeRepo().getOne(id);
			curnode.setName(postdata.get("name"));
			curnode.setObjectType(postdata.get("objectType"));
			if(postdata.get("objectId")!="") {
				curnode.setObjectId(Long.parseLong(postdata.get("objectId")));
			}
			if(postdata.get("recordId")!="") {
				curnode.setRecordId(Long.parseLong(postdata.get("recordId")));
			}
			service.getNodeRepo().save(curnode);
			return "redirect:/trees/display/" + curnode.getTree().getId().toString();
		}
		
		@PostMapping("/objectSearch")
		public String objectSearch(@RequestParam Map<String,String> postdata, Model model) {
			String searchType = postdata.get("searchType").toLowerCase();
			String tosearch = postdata.get("q");
			tosearch = "%" + tosearch.replaceAll(" ", "%") + "%";
			System.out.println("Searching:" + tosearch);
			if(searchType.equals("page")) {
				List<Page> pages = pageService.getRepo().findAllByQ(tosearch);
				model.addAttribute("pages",pages);
			}
			return "tree/node/object_search/" + searchType + ".html";
		}
}
