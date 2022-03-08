package org.portalengine.portal.entities;

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

import org.hibernate.annotations.Type;
import org.portalengine.portal.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tree_user")
public class TreeUser extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@JsonIgnore
	@ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "node_id" )
	private TreeNode node;
	
	@ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "user_id", columnDefinition = "numeric(19,0)" )
	@Type(type = "big_decimal")
	private User user;
	
	@NotNull
	private String role;
	
}
