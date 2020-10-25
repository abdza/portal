package org.portalengine.portal.FileLink;

import java.util.List;

import org.portalengine.portal.Page.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
	
	FileLink findOneByModuleAndSlug(String module,String slug);

	@Query("from FileLink pg where pg.slug like :#{#search} or pg.name like :#{#search}")
	List<FileLink> findAllByQ(String search);
	
	List<FileLink> findAllByModule(String module);
}
