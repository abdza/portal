package org.portalengine.portal.security;

import java.util.List;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.portalengine.portal.RepoCollection;
import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserRole;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class PortalSecurityExpressionRoot
		extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

	private Authentication authentication;
	private RepoCollection repos;
	private User curuser;

	public PortalSecurityExpressionRoot(Authentication authentication, RepoCollection repos) {
		super(authentication);
		this.authentication = authentication;
		this.repos = repos;
		this.curuser = null;
		SecurityUser secuser = null;
		Object userobj = this.authentication.getPrincipal();
		if (userobj == null) {
			System.out.println("User is anon");
		} else {
			if (userobj instanceof String) {
				this.curuser = repos.getUserRepository().findByUsername((String)userobj).orElse(null);
			} else if (userobj instanceof SecurityUser) {
				secuser = (SecurityUser) userobj;
				this.curuser = secuser.getUser();			
			} else if (userobj instanceof User) {
				this.curuser = (User) userobj;			
			}
		}
	}

	public List<UserRole> moduleRoles(String module) {
		List<UserRole> mr = null;
		if(curuser!=null){
			mr = this.repos.getUserRoleRepository().findByUserAndModuleIgnoreCase(curuser, module);
		}
		return mr;
	}

	public boolean trackerPermission(String module, String slug, String permission) {
		if(module==null) {
			module = "portal";
		}
		Tracker curtracker = repos.getTrackerRepository().findByModuleAndSlug(module, slug);

		if (curtracker == null) {
			return false;
		}

		List<UserRole> mr = moduleRoles(module);

		StringBuilder rlist = new StringBuilder();		

		if (permission.equals("list")) {
			rlist.append(curtracker.getViewListRoles());
		} else if (permission.equals("add")) {
			rlist.append(curtracker.getAddRoles());
		} else if (permission.equals("detail")) {
			rlist.append(curtracker.getDetailRoles());
		} else if (permission.equals("delete")) {
			rlist.append(curtracker.getDeleteRoles());
		} else if (permission.equals("edit")) {
			rlist.append(curtracker.getEditRoles());
		} else if (permission.equals("save")) {
			rlist.append(curtracker.getEditRoles()).append(",").append(curtracker.getAddRoles());
		}

		String rolelist = rlist.toString();

		if (rolelist == null || rolelist.length() == 0) {
			// if the role 'All' is not specified, then default to 'Authenticated' 
			rolelist = "Authenticated";
		}		

		for (String fname : rolelist.split(",")) {
			if (fname.trim().equals("All")) {
				return true;
			} else if (fname.trim().equals("None")) {
				return false;
			} else if (fname.trim().equals("Authenticated")) {
				if (curuser != null) {
					return true;
				}
			} else {
				// Need to check user roles here
				if (mr.size() > 0) {
					for (UserRole cr : mr) {
						if (fname.trim().equals(cr.getRole())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean pagePermission(String module, String slug) {
		if(module==null) {
			module = "portal";
		}
		PortalPage curpage = repos.getPageRepository().findOneByModuleAndSlug(module, slug);

		if (curpage == null) { 
			return false;
		}
		if (curpage.getPublished() != true) {
			return false;
		}
		if(curuser==null && curpage.getRequireLogin()) {
			return false;
		}

		List<UserRole> mr = moduleRoles(module);

		if(curpage.getAllowedRoles()!=null){
			for (String fname : curpage.getAllowedRoles().split(",")) {
				if (fname.trim().equals("All")) {
					return true;
				} else if (fname.trim().equals("None")) {
					return false;
				} else if (fname.trim().equals("Authenticated")) {
					if (curuser != null) {
						return true;
					}
				} else {
					// Need to check user roles here
					if (mr!=null && mr.size() > 0) {
						for (UserRole cr : mr) {
							if (fname.trim().equals(cr.getRole())) {
								return true;
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void setFilterObject(Object filterObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getFilterObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReturnObject(Object returnObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getReturnObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getThis() {
		// TODO Auto-generated method stub
		return null;
	}

}