package org.portalengine.portal.User.Task;

import java.util.List;

import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {
	
	List<UserTask> findByUser(User user);

}
