package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.DataUpdate;
import org.portalengine.portal.entities.Tracker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataUpdateRepository extends JpaRepository<DataUpdate, Long> {

	List<DataUpdate> findAllByTracker(Tracker tracker);

}
