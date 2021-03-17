package org.portalengine.portal;

import org.portalengine.portal.DataUpdate.DataUpdate;
import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkRepository;
import org.portalengine.portal.Page.PageRepository;
import org.portalengine.portal.Setting.SettingRepository;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerRepository;
import org.portalengine.portal.Tracker.Role.TrackerRoleRepository;
import org.portalengine.portal.Tree.TreeRepository;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.Role.UserRoleRepository;
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
