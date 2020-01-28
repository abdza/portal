package org.portalengine.portal.Setting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {

	Setting findOneByModuleAndName(String module,String name);
}
