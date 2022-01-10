package org.portalengine.portal.services;

import java.util.Date;

import org.portalengine.portal.entities.Setting;
import org.portalengine.portal.repositories.SettingRepository;
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
	
	public void SaveValue(String name, String module, String value) {
		Setting setting = repo.findOneByModuleAndName(module, name);
		if(setting!=null) {
			setting.setTextValue(value);
			repo.save(setting);
		}
	}
	
	public void SaveValue(String name, String module, Date value) {
		Setting setting = repo.findOneByModuleAndName(module, name);
		if(setting!=null) {
			setting.setDateValue(value);
			repo.save(setting);
		}
	}
	
	public void SaveValue(String name, String module, Long value) {
		Setting setting = repo.findOneByModuleAndName(module, name);
		if(setting!=null) {
			setting.setNumberValue(value);
			repo.save(setting);
		}
	}
	
}
