package portal.Setting;

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
	
}
