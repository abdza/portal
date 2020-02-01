package org.portalengine.portal.User;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findOneByStaffid(String staffid);
	
	User findByUsername(String username);

	@Query("select tt from User tt where tt.name like :#{#search} or tt.email like :#{#search} or tt.staffid like :#{#search}")
	List<User> apiquery(@Param("search") String search);
	
}
