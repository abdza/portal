package portal.Tracker.Field;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import portal.Tracker.Tracker;

public interface TrackerFieldRepository extends JpaRepository<TrackerField, Long> {

		List<TrackerField> findByTracker(Tracker tracker);
		TrackerField findByTrackerAndName(Tracker tracker, String name);
}
