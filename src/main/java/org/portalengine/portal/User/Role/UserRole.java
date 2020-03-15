package org.portalengine.portal.User.Role;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.User.User;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@Data
@Table(name = "portal_user_role")
@Entity
public class UserRole extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "user_id" )
	private User user;
	
	@NotNull
	private String role;
	
	@NotNull
	private String module;
	
	
}
