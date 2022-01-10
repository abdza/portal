package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.PortalPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PageRepository extends JpaRepository<PortalPage, Long> {

	PortalPage findOneByModuleAndSlug(String module,String slug);
	
	@Query("from PortalPage pg where pg.title like :#{#search} or pg.content like :#{#search} or pg.slug like :#{#search}")
	List<PortalPage> findAllByQ(String search);
	
	@Query("from PortalPage pg where pg.title like :#{#search} or pg.content like :#{#search} or pg.slug like :#{#search}")
	Page<PortalPage> apiquery(String search, Pageable pageable);
	
	@Query("from PortalPage pg where (pg.title like :#{#search} or pg.content like :#{#search} or pg.slug like :#{#search}) and pg.module=:#{#module}")
	Page<PortalPage> apimodulequery(String search, String module, Pageable pageable);
	
	List<PortalPage> findAllByModule(String module);
	
	@Query("from PortalPage pg where pg.module = :#{#module} and (pg.title like :#{#search} or pg.content like :#{#search} or pg.slug like :#{#search})")
	List<PortalPage> findAllByModuleAndQ(String module, String search);
}
