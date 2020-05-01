package org.portalengine.portal.Tree;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TreeNodeRepository extends JpaRepository<TreeNode, Long> {
	
	@Query("from TreeNode tn where tn.tree.id=:#{#tree.id} and tn.parent is null")
	TreeNode getRoot(Tree tree);
	
	TreeNode findFirstByFullPath(String fullPath);
	
	@Query("from TreeNode tn where tn.tree.id=:#{#node.tree.id} and tn.lft<=:#{#node.lft} and tn.rgt>=:#{#node.rgt} order by tn.lft")
	List<TreeNode> getPath(TreeNode node);
}