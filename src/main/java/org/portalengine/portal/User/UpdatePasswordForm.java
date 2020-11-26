package org.portalengine.portal.User;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdatePasswordForm {
	
	@NotNull
	@Size(min=8, max=30)
	private String password;
	
	@NotNull
	@Size(min=8, max=30)
	private String repeatPassword;
}
