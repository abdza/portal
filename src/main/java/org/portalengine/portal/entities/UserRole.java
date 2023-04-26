package org.portalengine.portal.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.hibernate.annotations.Type;
import org.portalengine.portal.Auditable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@Data
@Entity
@Table(name = UserRole.TABLE_NAME)
public class UserRole extends Auditable<String> {

	public static final String TABLE_NAME= "portal_user_role";
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	@JoinColumn( name = "user_id", columnDefinition = "bigserial" )
	@Type(type = "bigserial")
	@JsonBackReference
	private User user;
	
	@NotNull
	private String role;
	
	@NotNull
	private String module;
	
	@Override
	public String toString() {
		return this.module + " - " + this.role;
	}
	
}
