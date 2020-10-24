package org.portalengine.portal.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
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
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
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
	
	public void updatelisting() {		
		List<String> cmodules = new ArrayList<String>();
		SqlRowSet modules = jdbctemplate.queryForRowSet("select distinct module from portal_page");			
		while(modules.next()){
			String dmod = modules.getString(1);
			if(!cmodules.contains(dmod)){
				cmodules.add(dmod);
			}
		}
		modules = jdbctemplate.queryForRowSet("select distinct module from portal_file");			
		while(modules.next()){
			String dmod = modules.getString(1);
			if(!cmodules.contains(dmod)){
				cmodules.add(dmod);
			}
		}
		modules = jdbctemplate.queryForRowSet("select distinct module from portal_tracker");			
		while(modules.next()){
			String dmod = modules.getString(1);
			if(!cmodules.contains(dmod)){
				cmodules.add(dmod);
			}
		}
		modules = jdbctemplate.queryForRowSet("select distinct module from portal_setting");			
		while(modules.next()){
			String dmod = modules.getString(1);
			if(!cmodules.contains(dmod)){
				cmodules.add(dmod);
			}
		}
		modules = jdbctemplate.queryForRowSet("select distinct module from portal_tree");			
		while(modules.next()){
			String dmod = modules.getString(1);
			if(!cmodules.contains(dmod)){
				cmodules.add(dmod);
			}
		}
		System.out.println("cmodules:" + cmodules.toString());
		jdbctemplate.execute("delete from portal_module");
		cmodules.forEach(cmod -> {
			Module curmod = new Module();
			curmod.setName(cmod);
			repo.save(curmod);
		});
	}
	
}
