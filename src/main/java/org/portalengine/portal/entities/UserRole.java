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
@Table(name = "user_role")
@Entity
public class UserRole extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	@JoinColumn( name = "user_id", columnDefinition = "numeric(19,0)" )
	@Type(type = "big_decimal")
	@JsonBackReference
	private User user;
	
	@NotNull
	private String role;
	
	@NotNull
	private String module;
	
	@org.springframework.data.annotation.Version
	protected long version;
	
	@Override
	public String toString() {
		return this.module + " - " + this.role;
	}
	
}
