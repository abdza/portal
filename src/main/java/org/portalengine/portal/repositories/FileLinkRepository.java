package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Tracker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
	
	FileLink findOneByModuleAndSlug(String module,String slug);

	@Query("from FileLink pg where pg.slug like :#{#search} or pg.name like :#{#search}")
	List<FileLink> findAllByQ(String search);
	
	List<FileLink> findAllByModule(String module);
	
	@Query("from FileLink pg where pg.name like :#{#search} or pg.path like :#{#search} or pg.slug like :#{#search} or pg.fileGroup like :#{#search}")
	Page<FileLink> apiquery(String search, Pageable pageable);
	
	@Query("from FileLink pg where (pg.name like :#{#search} or pg.path like :#{#search} or pg.slug like :#{#search} or pg.fileGroup like :#{#search}) and pg.module=:#{#module}")
	Page<FileLink> apimodulequery(String search, String module, Pageable pageable);
}
