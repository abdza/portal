package org.portalengine.portal.Tracker.Field;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerFieldRepository extends JpaRepository<TrackerField, Long> {

		List<TrackerField> findByTracker(Tracker tracker);
		TrackerField findByTrackerAndName(Tracker tracker, String name);
}
