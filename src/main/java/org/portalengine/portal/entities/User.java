package org.portalengine.portal.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.portalengine.portal.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "IAP_User")
@JsonPropertyOrder({ "username", "staffid", "name", "email" })
@Data
public class User extends Auditable<String> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="new_staffid")
	private String username;

	@Column(name="StaffId")
	private String staffid;

	@Column(name="EMP_NAME")
	private String name;

	private String email;

	@Column(name="lanid")
	private String lan_id;

	@Column(name="IsActive")
	private Boolean isActive;
	
	@JsonIgnore
	private String password;
	
	@JsonIgnore
	private Boolean isAdmin;
	
	@JsonIgnore
	private Date sessionStart;
	
	@Column(name="date_joined")
	private Date dateRegister;
	
	public User(String username, String staffid, String name, String email, String password, Boolean isAdmin) {
		this.username = username;
		this.staffid = staffid;
		this.name = name;
		this.email = email;
		this.password = password;
		this.setIsAdmin(isAdmin);
	}
	
	public User() {
	}

	@OneToMany(
			fetch = FetchType.EAGER, 
			mappedBy = "user",
			orphanRemoval = true)
	@JsonManagedReference
	private List<UserRole> roles = new ArrayList<>();
	
	@JsonIgnore
	@PrePersist
	void dateRegister() {
		this.dateRegister = new Date();
	}
	
	@Override
	public String toString() {
		return this.name;
	}

}
