package org.portalengine.portal.controllers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.entities.Tree;
import org.portalengine.portal.entities.TreeNode;
import org.portalengine.portal.entities.TreeUser;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.services.TreeService;
import org.portalengine.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@RequestMapping("/api/trees")
public class TreeApiController {
	
	@Autowired
	private TreeService service;
	
	@Autowired
	private UserService userService;
	
	@GetMapping(value={"/nodequery","/nodequery/{tree_id}"})
	public Object nodequery(HttpServletRequest request, Model model, @PathVariable(required=false) Long tree_id) {
		Map<String, Object> map = new HashMap<String,Object>();
		int page = 0;
		int size = 20;
		Tree tree = null;
		if(tree_id!=null) {
			tree = service.getTreeRepo().getOne(tree_id);
		}
		if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
			page = Integer.parseInt(request.getParameter("page")) - 1;
		}
		if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
			size = Integer.parseInt(request.getParameter("size"));
		}
		String search = "";
		Page<TreeNode> toreturn = null;
		if(request.getParameter("q")!=null) {
			search = "%" + request.getParameter("q").replace(" " , "%") + "%";		
			Pageable pageable = PageRequest.of(page, size);
			if(tree!=null) {
				toreturn = service.getNodeRepo().apiquery(tree,search,pageable);
			}
			else {
				toreturn = service.getNodeRepo().apiquery(search,pageable);
			}
		}
		else {
			if(tree!=null) {
				Pageable pageable = PageRequest.of(page, size);
				System.out.println("Full tree for:" + tree.getName());
				toreturn = service.getNodeRepo().findAllByTree(tree,pageable);
			}
			else {
				toreturn = service.getNodeRepo().findAll(PageRequest.of(page, size));
			}
		}
		
		ArrayList<TreeNode> nodelist = new ArrayList<TreeNode>();
		for(int i=0;i<toreturn.getContent().size();i++) {
			TreeNode cu = toreturn.getContent().get(i);
			nodelist.add(cu);
		}
		map.put("content", nodelist);
		return map;
	}

	@GetMapping
	public Object list(HttpServletRequest request, Model model) {
		Map<String, Object> map = new HashMap<String,Object>();
		int page = 0;
		int size = 20;
		if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
			page = Integer.parseInt(request.getParameter("page")) - 1;
		}
		if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
			size = Integer.parseInt(request.getParameter("size"));
		}
		String search = "";
		Page<Tree> toreturn = null;
		if(request.getParameter("q")!=null) {
			search = "%" + request.getParameter("q").replace(" " , "%") + "%";		
			Pageable pageable = PageRequest.of(page, size);
			toreturn = service.getTreeRepo().apiquery(search,pageable);
		}
		else {
			toreturn = service.getTreeRepo().findAll(PageRequest.of(page, size));
		}
		
		ArrayList<Tree> treelist = new ArrayList<Tree>();
		for(int i=0;i<toreturn.getContent().size();i++) {
			Tree cu = toreturn.getContent().get(i);
			treelist.add(cu);
		}
		map.put("content", treelist);
		return map;
	}
	
	@PostMapping("/nodes/move")
	public String moveNode(Model model, HttpServletRequest request) {
		Map<String, String[]> postdata = request.getParameterMap();
		TreeNode parentnode = service.getNodeRepo().getOne(Long.parseLong(postdata.get("parent_id")[0]));
		TreeNode currentnode = service.getNodeRepo().getOne(Long.parseLong(postdata.get("node_id")[0]));
		System.out.println("parent:" + parentnode.getId().toString() + " node:" + currentnode.getId().toString() + " move:" + postdata.get("position")[0]);
		service.moveNode(parentnode, currentnode, postdata.get("position")[0]);
		return "redirect:/trees/display/" + parentnode.getTree().getId().toString();
	}
	
	@PostMapping(value = "/nodes/saveuser", consumes = "application/x-www-form-urlencoded")
	public TreeNode saveuser(HttpServletRequest request) {
		Map<String, String[]> postdata = request.getParameterMap();
		String node_id = postdata.get("node_id")[0];
		String user_id = postdata.get("user_id")[0];
		String role = postdata.get("role")[0];
		TreeUser tuser = new TreeUser();
		TreeNode tnode = service.getNodeRepo().findById(Long.parseLong(node_id)).orElse(null);
		if(tnode!=null) {
			tuser.setNode(tnode);
			User user = userService.getRepo().findById(Long.parseLong(user_id)).orElse(null);
			tuser.setUser(user);
			tuser.setRole(role);
			service.getUserRepo().save(tuser);
			return tnode;
		}
		return null;
	}
	
	@GetMapping("/{id}")
	public Object display(@PathVariable Long id, Model model) {
		Tree curtree = service.getTreeRepo().getOne(id);
		ArrayList<Object> children = new ArrayList<>();
		children.add(nodeJson(service.getRoot(curtree)));
		return children;
	}
	
	@PostMapping("/node/{node_id}/delete")
	public Long deleteTreeNode(@PathVariable Long node_id) {
		TreeNode curnode = service.getNodeRepo().findById(node_id).orElse(null);
		Long parent_id = null;
		if(curnode!=null) {
			parent_id = curnode.getParent().getId();
		}
		service.deleteNode(curnode);
		return parent_id;
	}
	
	@GetMapping("/node/{node_id}")
	public TreeNode treeNode(@PathVariable Long node_id) {
		TreeNode curnode = service.getNodeRepo().findById(node_id).orElse(null);
		return curnode;
	}
	
	public Object nodeJson(TreeNode current) {
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("key", current.getId());
		map.put("title",current.getName());
		map.put("objectType", current.getObjectType());
		map.put("objectId", current.getObjectId());
		map.put("recordId", current.getRecordId());
		map.put("rootLessPath", service.rootLessPath(current));
		map.put("slug", current.getSlug());
		map.put("fullPath", current.getFullPath());
		map.put("status", current.getStatus());
		map.put("nodedata", current.getData());
		map.put("lftrgt", current.getLft().toString() + "/" + current.getRgt().toString());
		ArrayList<Object> children = new ArrayList<>();
		for(int i=0; i<current.getChildren().size();i++) {
			children.add(nodeJson(current.getChildren().get(i)));
		}
		if(current.getChildren().size()>0) {
			map.put("folder", true);
			map.put("children", children);
		}
		return map;
	}
	
}
