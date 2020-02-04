package org.portalengine.portal.Tree;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class TreeService {

	@Autowired
	private TreeRepository treeRepo;
	
	@Autowired
	private TreeNodeRepository nodeRepo;
	
	@Autowired
	private TreeUserRepository userRepo;
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private ServletContext servletContext;
	
	private final static String TEMPLATE_LOCAL = "US";
	
	@Autowired
	public TreeService() {
	}
}
