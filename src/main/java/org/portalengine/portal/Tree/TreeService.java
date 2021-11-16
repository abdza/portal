package org.portalengine.portal.Tree;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.portalengine.portal.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

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
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	private final static String TEMPLATE_LOCAL = "US";
	
	@Autowired
	public TreeService() {
	}
	
	public List<String> userRoles(User user,TreeNode node) {
		return userRoles(user,node,true);
	}
	
	@Value("${rootnode:portal}") String rootnode;
	
	public TreeNode getNodeFromPath(String path) {
		TreeNode toreturn = null;
		Tree portaltree = treeRepo.findOneByModuleAndSlug("portal", "portal");
		if(portaltree!=null) {
			String[] slugs = path.split("/");
			
			/* Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			
			if (!(auth instanceof AnonymousAuthenticationToken)) {
			        // userDetails = auth.getPrincipal()
				UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				List<String> userRoles = userRoles((User)userDetails, pnode);
				List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
				List<GrantedAuthority> currentAuthorities = new ArrayList<>(auth.getAuthorities());
				for(final GrantedAuthority crole:currentAuthorities) {
					if(!crole.getAuthority().contains("ROLE_NODE_")) {
						updatedAuthorities.add(crole);
					}
				}
				if(userRoles.size()>0) {			
					for(final String crole:userRoles) {
						updatedAuthorities.add(new SimpleGrantedAuthority(crole)); //add your role here [e.g., new SimpleGrantedAuthority("ROLE_NEW_ROLE")]
					}					
				}
				Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
				SecurityContextHolder.getContext().setAuthentication(newAuth);
			} */
			
			for(int i=0; i<slugs.length; i++) {
				if(i==0) {
					toreturn = nodeRepo.getRoot(portaltree);
				}
				else {
					TreeNode curnode = nodeRepo.findBySlugAndParent(slugs[i], toreturn);
					if(curnode!=null) {
						toreturn = curnode;
					}
				}
			}
		}
		return toreturn;
	}
	
	public String rootLessPath(TreeNode tnode) {
		String toreturn = tnode.getFullPath().replaceFirst(rootnode + "/", "/");
		if(tnode.getFullPath().equals(rootnode)) {
			toreturn = "";
		}
		return toreturn;
	}
	
	public String portalPath(TreeNode tnode) {
		String toreturn = "";
		String rlp = rootLessPath(tnode);
		if(rlp.length()>0 && rlp.charAt(0)=='/') {
			toreturn = "p" + rlp;
		}
		else {
			if(rlp.length()>0) {
				toreturn = "p/" + rlp;
			}
			else {
				toreturn = "p";
			}
		}
		return toreturn;
	}
	
	public List<String> userRoles(User user,TreeNode node,Boolean fullPath){
		List<TreeUser> foundUserRoles;
		if(fullPath) {
			foundUserRoles = userRepo.findRolesForFullNode(user,node);
		}
		else {
			foundUserRoles = userRepo.findRolesForNode(user,node);
		}
		ArrayList<String> retroles = new ArrayList<String>();
		for(final TreeUser ctu : foundUserRoles) {
			String transform = "ROLE_NODE_" + ctu.getRole().toUpperCase();
			retroles.add(transform);
		}
		return retroles;
	}
	
	public void saveTree(Tree tree) {
		treeRepo.save(tree);
		if(getRoot(tree)==null) {
			TreeNode root = new TreeNode();
			root.setName(tree.getName());
			root.setSlug(slugify(tree.getName()));
			root.setFullPath(slugify(tree.getName()));
			root.setTree(tree);
			root.setLft((long) 1);
			root.setRgt((long) 2);
			nodeRepo.save(root);
		}
	}
	
	public TreeNode getRoot(Tree tree) {
		TreeNode root = nodeRepo.getRoot(tree);
		return root;
	}
	
	public void moveNode(TreeNode parent, TreeNode curnode, String position) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();	
		Long nodesize = curnode.getRgt() - curnode.getLft();
		Long moveby = curnode.getRgt() + 1;
		Long curlft = curnode.getLft();
		
		//First step is to move the current node outside of the tree by settings all lft and rgt below 0
		paramsource.addValue("tree_id",curnode.getTree().getId());
		paramsource.addValue("curlft",curnode.getLft());
		paramsource.addValue("currgt",curnode.getRgt());
		paramsource.addValue("curid",curnode.getId());
		paramsource.addValue("parentid", parent.getId());
		
		paramsource.addValue("moveby",moveby);
		paramsource.addValue("nodesize",nodesize+1);
		namedjdbctemplate.update("update portal_tree_node set lft=lft-:moveby,rgt=rgt-:moveby where lft>=:curlft and rgt<=:currgt and tree_id=:tree_id", paramsource);
		
		//Second step is to close the gap caused by the move
		namedjdbctemplate.update("update portal_tree_node set lft=lft-:nodesize where lft>=:curlft and tree_id=:tree_id", paramsource);
		namedjdbctemplate.update("update portal_tree_node set rgt=rgt-:nodesize where rgt>=:curlft and tree_id=:tree_id", paramsource);
		
		TreeNode prefresh = nodeRepo.getOne(parent.getId());
		paramsource.addValue("parentlft", prefresh.getLft());
		paramsource.addValue("parentrgt", prefresh.getRgt());
		//Third step is to make space where to be inserted		
		if(position.equals("over")) {
			System.out.println("doing over");
			namedjdbctemplate.update("update portal_tree_node set lft=lft+:nodesize where lft>=:parentrgt and tree_id=:tree_id", paramsource);
			namedjdbctemplate.update("update portal_tree_node set rgt=rgt+:nodesize where rgt>=:parentrgt and tree_id=:tree_id", paramsource);
			paramsource.addValue("newlft", prefresh.getRgt());
			paramsource.addValue("parentid", prefresh.getId());
		}
		else if(position.equals("before")) {
			System.out.println("doing before");
			namedjdbctemplate.update("update portal_tree_node set lft=lft+:nodesize where lft>=:parentlft and tree_id=:tree_id", paramsource);
			namedjdbctemplate.update("update portal_tree_node set rgt=rgt+:nodesize where rgt>=:parentlft and tree_id=:tree_id", paramsource);
			paramsource.addValue("newlft", prefresh.getLft());
			paramsource.addValue("parentid", prefresh.getParent().getId());
		}
		else if(position.equals("after")) {
			System.out.println("doing after");
			namedjdbctemplate.update("update portal_tree_node set lft=lft+:nodesize where lft>=:parentrgt + 1 and tree_id=:tree_id", paramsource);
			namedjdbctemplate.update("update portal_tree_node set rgt=rgt+:nodesize where rgt>=:parentrgt + 1 and tree_id=:tree_id", paramsource);
			paramsource.addValue("newlft", prefresh.getRgt() + 1);
			paramsource.addValue("parentid", prefresh.getParent().getId());
		}
		
		//Fourth step is to move over the node into the proper position
		namedjdbctemplate.update("update portal_tree_node set lft=lft+:newlft+:nodesize,rgt=rgt+:newlft+:nodesize where lft<0 and tree_id=:tree_id", paramsource);
		
		//Update parent node
		namedjdbctemplate.update("update portal_tree_node set parent_id=:parentid where id=:curid", paramsource);
		
	}
	
	public String slugify(String name) {
		String toreturn = name.replaceAll("[^a-zA-Z0-9\\s]","").replaceAll(" ","_").toLowerCase();
		return toreturn;
	}
	
	public String nextSlug(String curslug, TreeNode curnode) {
		String validslug = slugify(curslug);
		String validfull = curnode.getFullPath() + "/" + validslug;
		TreeNode pnode = nodeRepo.findFirstByFullPath(validfull);
		while(pnode!=null) {
			validslug = validslug + "c";
			validfull = pnode.getFullPath() + "c";
			pnode = nodeRepo.findFirstByFullPath(validfull);
		}
		return validslug;
	}
	
	public TreeNode addNode(TreeNode node, String name, String position) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();		
		
		position = "last";
		TreeNode newnode = new TreeNode();
		String validslug = nextSlug(slugify(name),node);
				
		newnode.setName(name);
		newnode.setSlug(validslug);
		newnode.setFullPath(node.getFullPath() + "/" + validslug);
		newnode.setTree(node.getTree());
		newnode.setLft(node.getRgt());
		newnode.setRgt(newnode.getLft()+1);		
		
		newnode.setParent(node);
		
		paramsource.addValue("crgt", newnode.getLft());
		paramsource.addValue("tree_id",node.getTree().getId());
		namedjdbctemplate.update("update portal_tree_node set lft=lft+2 where lft>=:crgt and tree_id=:tree_id", paramsource);
		namedjdbctemplate.update("update portal_tree_node set rgt=rgt+2 where rgt>=:crgt and tree_id=:tree_id", paramsource);
		nodeRepo.save(newnode);		
		return newnode;
	}
	
	public boolean deleteNode(TreeNode node) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();		
		
		paramsource.addValue("crgt", node.getRgt());
		paramsource.addValue("clft", node.getLft());
		paramsource.addValue("tree_id",node.getTree().getId());
		namedjdbctemplate.update("delete from portal_tree_node where tree_id=:tree_id and lft>=:clft and rgt<=:crgt", paramsource);
		return true;
	}
	
	public List<TreeNode> getPath(TreeNode node) {
		return nodeRepo.getPath(node);
	}
	
	public String pathString(TreeNode node) {
		List<TreeNode> path = getPath(node);
		String ret = "";
		for(TreeNode cn:path) {
			ret += cn.getId().toString() + "/";
		}
		return ret;
	}
	
	public List<TreeNode> publishedChildren(TreeNode node) {
		System.out.println("In published children");
		return nodeRepo.publishedChildren(node);
	}
	
	public void fixTree(Tree tree) {
		TreeNode root = this.nodeRepo.getRoot(tree);
		root.setLft((long)1);
		root.setRgt(this.fixChildren(root,(long)1));
		this.nodeRepo.save(root);		
	}
	
	public Long fixChildren(TreeNode node,Long lft) {
		List<TreeNode> childrens = this.nodeRepo.findChildren(node);
		Long curlft = lft;
		Long currgt = lft;
		for(int i=0;i<childrens.size();i++) {
			curlft = currgt + 1;
			TreeNode curchild = childrens.get(i);
			curchild.setLft(curlft);
			if(this.nodeRepo.findChildren(curchild).size()>0) {
				currgt = fixChildren(curchild,curlft);
			}
			else {
				currgt = curlft + 1;
			}
			curchild.setRgt(currgt);
			this.nodeRepo.save(curchild);
		}
		return currgt;
	}
}
