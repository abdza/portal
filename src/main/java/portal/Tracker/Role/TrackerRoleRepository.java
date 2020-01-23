package portal.Tracker.Role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import portal.Tracker.Tracker;

public interface TrackerRoleRepository extends JpaRepository<TrackerRole, Long> {

		List<TrackerRole> findByTracker(Tracker tracker);
		TrackerRole findByTrackerAndName(Tracker tracker, String name);
		
}
