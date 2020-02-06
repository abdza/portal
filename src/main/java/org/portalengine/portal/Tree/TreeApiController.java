package org.portalengine.portal.Tree;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.portalengine.portal.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@RequestMapping("/api/trees")
public class TreeApiController {
	
	@Autowired
	private TreeService service;

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
	
	@GetMapping("/{id}")
	public Object display(@PathVariable Long id, Model model) {
		Tree curtree = service.getTreeRepo().getOne(id);
		System.out.println("Current tree:" + curtree.getName());
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("text",curtree.getName());
		return map;
	}
	
}
