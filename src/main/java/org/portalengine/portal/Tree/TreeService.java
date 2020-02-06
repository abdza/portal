package org.portalengine.portal.Tree;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
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
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	private final static String TEMPLATE_LOCAL = "US";
	
	@Autowired
	public TreeService() {
	}
	
	public void saveTree(Tree tree) {
		if(tree.getRoot()==null) {
			TreeNode root = new TreeNode();
			root.setName(tree.getName());
			root.setTree(tree);
			root.setLft((long) 1);
			root.setRgt((long) 2);
			tree.setRoot(root);
		}
		treeRepo.save(tree);
		
		addNode(tree.getRoot(),tree.getName() + " other","last");
	}
	
	public void addNode(TreeNode node, String name, String position) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();		
		
		position = "last";
		TreeNode newnode = new TreeNode();
		newnode.setName(name);
		newnode.setTree(node.getTree());
		newnode.setLft(node.getRgt());
		newnode.setRgt(newnode.getLft()+1);
		newnode.setParent(node);
		
		paramsource.addValue("crgt", newnode.getLft());
		paramsource.addValue("tree_id",node.getTree().getId());
		namedjdbctemplate.update("update portal_tree_node set lft=lft+2 where lft>=:crgt and tree_id=:tree_id", paramsource);
		namedjdbctemplate.update("update portal_tree_node set rgt=rgt+2 where rgt>=:crgt and tree_id=:tree_id", paramsource);
		nodeRepo.save(newnode);		
	}
}
