package org.portalengine.portal.User;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
	
	@Data
	private class ApiUser {
		public ApiUser(String name2, String staffid2, String email2) {
			// TODO Auto-generated constructor stub
			this.name=name2;
			this.staffid=staffid2;
			this.email=email2;
		}
		String name;
		String staffid;
		String email;
	}
	
	@Autowired
	private UserService service;

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
		Page<User> toreturn = null;
		if(request.getParameter("q")!=null) {
			search = "%" + request.getParameter("q").replace(" " , "%") + "%";		
			Pageable pageable = PageRequest.of(page, size);
			toreturn = service.getRepo().apiquery(search,pageable);
		}
		else {
			toreturn = service.getRepo().findAll(PageRequest.of(page, size));
		}
		
		ArrayList<ApiUser> userlist = new ArrayList<ApiUser>();
		for(int i=0;i<toreturn.getContent().size();i++) {
			User cu = toreturn.getContent().get(i);
			userlist.add(new ApiUser(cu.getName(),cu.getStaffid(),cu.getEmail()));
		}
		map.put("content", userlist);
		return map;
	}
}
