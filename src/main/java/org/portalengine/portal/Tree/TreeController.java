package org.portalengine.portal.Tree;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trees")
public class TreeController {
	
		@Autowired
		private TreeService service;
		
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
			return "tree/display.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Tree tree,Model model) {
			service.saveTree(tree);
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
			return "tree/node/edit.html";
		}
		
		@PostMapping("/nodes/{id}/update")
		public String updateNode(@PathVariable Long id, HttpServletRequest request,Model model) {
			Map<String, String[]> postdata = request.getParameterMap();
			TreeNode curnode = service.getNodeRepo().getOne(id);
			curnode.setName(postdata.get("name")[0]);
			service.getNodeRepo().save(curnode);
			return "redirect:/trees/display/" + curnode.getTree().getId().toString();
		}
}
