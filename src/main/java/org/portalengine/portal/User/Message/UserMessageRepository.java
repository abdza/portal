package org.portalengine.portal.User.Message;

import java.util.List;

import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
	
	List<UserMessage> findByUser(User user);

}
