package org.portalengine.portal.Setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {

	@Autowired
	private SettingRepository repo;
	
	@Autowired
	public SettingService() {
	}

	public SettingRepository getRepo() {
		return repo;
	}

	public void setRepo(SettingRepository repo) {
		this.repo = repo;
	}
	
	public String StringSetting(String name) {
		return StringSetting(name,null);
	}
	
	public String StringSetting(String name, String defvalue) {
		return StringSetting(name,"portal",defvalue);
	}
	
	public String StringSetting(String name, String module, String defvalue) {
		Setting setting = repo.findOneByModuleAndName(module, name);
		if(setting!=null) {
			return setting.getTextValue();
		}
		return defvalue;
	}
	
}
