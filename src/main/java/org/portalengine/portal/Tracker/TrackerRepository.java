package org.portalengine.portal.Tracker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {

	Tracker findOneByModuleAndSlug(String module,String slug);
}
