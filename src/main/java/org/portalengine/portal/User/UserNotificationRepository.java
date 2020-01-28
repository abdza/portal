package org.portalengine.portal.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
	
	List<UserNotification> findByUser(User user);

}
