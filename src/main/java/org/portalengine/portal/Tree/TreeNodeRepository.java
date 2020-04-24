package org.portalengine.portal.Tree;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TreeNodeRepository extends JpaRepository<TreeNode, Long> {
	
	@Query("from TreeNode tn where tn.tree.id=:#{#tree.id} and tn.parent is null")
	TreeNode getRoot(Tree tree);
	
	TreeNode findByFullpath(String fullpath);
}