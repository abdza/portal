package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerField;
import org.portalengine.portal.entities.TrackerRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerRoleRepository extends JpaRepository<TrackerRole, Long> {

		List<TrackerRole> findByTracker(Tracker tracker);
		TrackerRole findByTrackerAndName(Tracker tracker, String name);
		List<TrackerRole> findByTrackerAndNameContainingIgnoreCase(Tracker tracker, String name);
}
