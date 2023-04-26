package org.portalengine.portal.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.portalengine.portal.Auditable;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

@Data
@Entity
@Table(name = Tree.TABLE_NAME)
public class Tree extends Auditable<String> {

	public static final String TABLE_NAME= "portal_tree";

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String module;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String name;
	
	@OneToMany(
			mappedBy = "tree",
			orphanRemoval = true)
	private List<TreeNode> nodes = new ArrayList<>();
	
}
