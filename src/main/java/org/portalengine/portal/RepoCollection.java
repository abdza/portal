package org.portalengine.portal;

import org.portalengine.portal.entities.DataUpdate;
import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.repositories.FileLinkRepository;
import org.portalengine.portal.repositories.PageRepository;
import org.portalengine.portal.repositories.SettingRepository;
import org.portalengine.portal.repositories.TrackerRepository;
import org.portalengine.portal.repositories.TrackerRoleRepository;
import org.portalengine.portal.repositories.TreeRepository;
import org.portalengine.portal.repositories.UserRoleRepository;
import org.springframework.security.core.Authentication;

import lombok.Data;

@Data
public class RepoCollection {
	
	private Authentication authentication;	
	private PageRepository pageRepository;
	private FileLinkRepository fileRepository;
	private	TrackerRepository trackerRepository;
	private TreeRepository treeRepository;
	private	SettingRepository settingRepository;
	private TrackerRoleRepository trackerRoleRepository;
	private UserRoleRepository userRoleRepository;

}
