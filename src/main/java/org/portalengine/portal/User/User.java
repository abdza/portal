package org.portalengine.portal.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.portalengine.portal.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Table(name = "portal_user")
@NoArgsConstructor(access=AccessLevel.PACKAGE, force=true)
public class User extends Auditable<String> implements UserDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	@Transient
	private PasswordEncoder passwordEncoder;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String username;
	private String staffid;
	private String name;
	private String email;
	private String password;
	private Boolean isAdmin;
	private Date dateRegister;
	
	public User(String username, String staffid, String name, String email, String password, Boolean isAdmin) {
		this.username = username;
		this.staffid = staffid;
		this.name = name;
		this.email = email;
		this.password = password;
		this.isAdmin = isAdmin;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		System.out.println("In get authorities");
		List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		if(this.isAdmin!=null && this.isAdmin) {
			authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));
		}
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@PrePersist
	void dateRegister() {
		this.dateRegister = new Date();
	}

}
