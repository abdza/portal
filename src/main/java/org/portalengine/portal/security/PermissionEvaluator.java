package org.portalengine.portal.security;

import java.io.Serializable;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permissionEvaluator")
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		System.out.println("in target domain");
		if (authentication != null && permission instanceof String) {
			
		}
		
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		System.out.println("in serializable");
		// TODO Auto-generated method stub
		return false;
	}

}
