package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
	
	List<UserMessage> findByUser(User user);

}
