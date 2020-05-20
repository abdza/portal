package org.portalengine.portal.Tree;

import java.util.List;

import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TreeUserRepository extends JpaRepository<TreeUser, Long> {
	
	@Query("from TreeUser tt where tt.user=:#{#user} and tt.node=:#{#node}")
	List<TreeUser> findRolesForNode(User user, TreeNode node);

}