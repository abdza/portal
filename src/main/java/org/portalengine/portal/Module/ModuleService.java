package org.portalengine.portal.Module;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Service
public class ModuleService {

	@Autowired
	private ModuleRepository repo;
	
	@Autowired
	private SpringTemplateEngine templateEngine;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private ServletContext servletContext;
	
	private final static String TEMPLATE_LOCAL = "US";
	
	@Autowired
	public ModuleService() {
	}

	public ModuleRepository getRepo() {
		return repo;
	}

	public void setRepo(ModuleRepository repo) {
		this.repo = repo;
	}
	
}
