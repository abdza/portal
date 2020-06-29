package org.portalengine.portal.Tree;

import java.util.List;

import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TreeUserRepository extends JpaRepository<TreeUser, Long> {
	
	@Query("from TreeUser tt where tt.user=:#{#user} and tt.node=:#{#node}")
	List<TreeUser> findRolesForNode(User user, TreeNode node);
	
	@Query("from TreeUser tt where tt.user=:#{#user} and tt.node.tree.id=:#{#node.tree.id} and tt.node.lft<=:#{#node.lft} and tt.node.rgt>=:#{#node.rgt}")
	List<TreeUser> findRolesForFullNode(User user, TreeNode node);

}