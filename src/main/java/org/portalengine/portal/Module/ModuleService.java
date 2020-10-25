package org.portalengine.portal.Module;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Setting.Setting;
import org.portalengine.portal.Setting.SettingService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
import org.portalengine.portal.Tree.Tree;
import org.portalengine.portal.Tree.TreeService;
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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ModuleService {

	@Autowired
	private ModuleRepository repo;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private TreeService treeService;		
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private SettingService settingService;
	
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
	
	public void importModule(String module) {
		ObjectMapper objectMapper = new ObjectMapper();
		String cwd = new File("").getAbsolutePath() + "/custom_modules";
		String prepend = settingService.StringSetting("module_folder",cwd);
		String mod_path = prepend + "/" + module + "/";
		File mpf = new File(mod_path);
		if(!mpf.exists()) {
			mpf.mkdirs();
		}
		
		List<Page> pages = null;
		try {
			pages = objectMapper.readValue(new File(mod_path + "pages.json"), new TypeReference<List<Page>>() {});
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("pages:" + pages.toString());
		
		List<FileLink> files = null;
		try {
			files = objectMapper.readValue(new File(mod_path + "files.json"), new TypeReference<List<FileLink>>() {});
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		List<Tracker> trackers = null;
		try {
			 trackers = objectMapper.readValue(new File(mod_path + "trackers.json"), new TypeReference<List<Tracker>>() {});
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		List<Setting> settings = null;
		try {
			settings = objectMapper.readValue(new File(mod_path + "settings.json"), new TypeReference<List<Setting>>() {});
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		/* List<Tree> trees = null;
		try {
			trees = objectMapper.readValue(new File(mod_path + "trees.json"), new TypeReference<List<Tree>>() {});
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/	
	}
	
	public void exportModule(String module) {
		ObjectMapper objectMapper = new ObjectMapper();
		String cwd = new File("").getAbsolutePath() + "/custom_modules";
		String prepend = settingService.StringSetting("module_folder",cwd);
		String mod_path = prepend + "/" + module + "/";
		File mpf = new File(mod_path);
		if(!mpf.exists()) {
			mpf.mkdirs();
		}
		
		List<Page> pages = pageService.getRepo().findAllByModule(module);
		try {
			objectMapper.writeValue(new File(mod_path + "pages.json"), pages);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<FileLink> files = fileService.getRepo().findAllByModule(module);
		try {
			objectMapper.writeValue(new File(mod_path + "files.json"), files);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Tracker> trackers = trackerService.getRepo().findAllByModule(module);
		try {
			objectMapper.writeValue(new File(mod_path + "trackers.json"), files);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Setting> settings = settingService.getRepo().findAllByModule(module);
		try {
			objectMapper.writeValue(new File(mod_path + "settings.json"), files);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Tree> trees = treeService.getTreeRepo().findAllByModule(module);
		try {
			objectMapper.writeValue(new File(mod_path + "trees.json"), files);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
