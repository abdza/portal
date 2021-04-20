package org.portalengine.portal.Setting;

import java.util.List;

import org.portalengine.portal.FileLink.FileLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettingRepository extends JpaRepository<Setting, Long> {

	Setting findOneByModuleAndName(String module,String name);
	
	List<Setting> findAllByModule(String module);
	
	@Query("from Setting pg where pg.name like :#{#search} or pg.textValue like :#{#search}")
	Page<Setting> apiquery(String search, Pageable pageable);
	
	@Query("from Setting pg where (pg.name like :#{#search} or pg.textValue like :#{#search}) and pg.module=:#{#module}")
	Page<Setting> apimodulequery(String search, String module, Pageable pageable);
}
