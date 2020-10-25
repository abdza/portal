package org.portalengine.portal.Tracker;

import java.util.List;

import org.portalengine.portal.Page.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {

	Tracker findOneByModuleAndSlug(String module,String slug);
	
	@Query("from Tracker pg where pg.name like :#{#search} or pg.dataTable like :#{#search} or pg.slug like :#{#search}")
	List<Tracker> findAllByQ(String search);
	
	List<Tracker> findAllByModule(String module);
}
