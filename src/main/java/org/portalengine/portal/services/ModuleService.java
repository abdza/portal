package org.portalengine.portal.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.math3.linear.NonPositiveDefiniteOperatorException;
import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.entities.Module;
import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Setting;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerField;
import org.portalengine.portal.entities.Tree;
import org.portalengine.portal.entities.TreeNode;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.repositories.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
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
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.var;

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
	private UserService userService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private Environment env;
	
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
	
	public TreeNode fixNode(TreeNode node,Tree tree) {
		node.setId(null);
		node.setTree(tree);
		node.getChildren().forEach(cnode->{
			cnode = fixNode(cnode,tree);			
		});
		node.getUsers().forEach(cuser->{
			var o = userService.getRepo().findByUsername(cuser.getUser().getUsername());
			User duser = o.orElseThrow(() -> new UsernameNotFoundException("Username not found"));
			if(duser!=null) {
				cuser.setUser(duser);				
			}
			else {
				cuser = null;
			}
		});
		return node;
	}
	
	public void uploadModule(MultipartFile file) {
		String cwd = new File("").getAbsolutePath() + "/custom_modules";
		String prepend = settingService.StringSetting("module_folder",cwd);
		File mpf = new File(prepend);
		if(!mpf.exists()) {
			mpf.mkdirs();
		}
		String filepath = prepend + "/" + file.getOriginalFilename();
		try {
			file.transferTo(new File(filepath));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(filepath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(prepend + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                if(ze.isDirectory()) {
                	newFile.mkdirs();
                }
                else {
                	new File(newFile.getParent()).mkdirs();
                	FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                    }
                    fos.close();
                }                
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
		
		List<PortalPage> pages = null;
		try {
			File pagefile = new File(mod_path + "pages.json");
			if(pagefile.exists()){
				pages = objectMapper.readValue(pagefile, new TypeReference<List<PortalPage>>() {});
				if(pages.size()>0) {
					pages.forEach(page -> {					
						PortalPage curp = pageService.getRepo().findOneByModuleAndSlug(page.getModule(), page.getSlug());
						if(curp!=null) {
							page.setId(curp.getId());
						}
						pageService.getRepo().save(page);					
					});
					pageService.getRepo().flush();
				}
			}
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
		
		List<FileLink> files = null;
		try {
			File filefile = new File(mod_path + "files.json");
			if(filefile.exists()) {
				files = objectMapper.readValue(filefile, new TypeReference<List<FileLink>>() {});
				if(files.size()>0) {
					files.forEach(cfile -> {
						FileLink ccfile = fileService.getRepo().findOneByModuleAndSlug(cfile.getModule(), cfile.getSlug());
						if(ccfile!=null) {
							cfile.setId(ccfile.getId());
						}
						fileService.getRepo().save(cfile);
					});
					fileService.getRepo().flush();
				}
			}
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
			File trackerfile = new File(mod_path + "trackers.json");
			if(trackerfile.exists()) {
				 trackers = objectMapper.readValue(trackerfile, new TypeReference<List<Tracker>>() {});
				 if(trackers.size()>0) {
					 trackers.forEach(ctracker -> {
						 Tracker cctracker = trackerService.getRepo().findByModuleAndSlug(ctracker.getModule(), ctracker.getSlug());
						 if(cctracker!=null) {
							 ctracker.setId(cctracker.getId());
						 }
						 List<TrackerField> fields = new ArrayList<TrackerField>();
						 ctracker.getFields().forEach(cfield -> {						 
							 if(cctracker!=null) {
								 TrackerField prevfield = trackerService.getFieldRepo().findByTrackerAndName(cctracker, cfield.getName());
								 if(prevfield!=null) {
									 cfield.setId(prevfield.getId());
								 }
								 else {
									 cfield.setId(null);
								 }
							 }						 
							 fields.add(cfield);
						 });
						 if(cctracker==null) {
							 ctracker.setFields(null);
						 }
						 trackerService.getRepo().save(ctracker);
						 trackerService.getRepo().flush();
						 final Tracker fctracker = trackerService.getRepo().findByModuleAndSlug(ctracker.getModule(), ctracker.getSlug());					 
						 fields.forEach(cfield -> {
							 cfield.setTracker(fctracker);						 
							 trackerService.getFieldRepo().save(cfield);						
						 });
						 trackerService.getFieldRepo().flush();					 
					 });
				 }
			 }
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
			File settingfile = new File(mod_path + "settings.json");
			if(settingfile.exists()) {
				settings = objectMapper.readValue(settingfile, new TypeReference<List<Setting>>() {});
				if(settings.size()>0) {
					settings.forEach(csetting -> {
						Setting ccsetting = settingService.getRepo().findOneByModuleAndName(csetting.getModule(), csetting.getName());
						if(ccsetting!=null) {
							csetting.setId(ccsetting.getId());
						}
						settingService.getRepo().save(csetting);
					});
					settingService.getRepo().flush();
				}
			}
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
			
		List<Tree> trees = null;
		try {
			File treefile = new File(mod_path + "trees.json");
			if(treefile.exists()) {
				trees = objectMapper.readValue(treefile, new TypeReference<List<Tree>>() {});
				if(trees.size()>0) {
					trees.forEach(ctree -> {
						Tree cctree = treeService.getTreeRepo().findByModuleAndSlug(ctree.getModule(), ctree.getSlug());
						if(cctree!=null) {
							ctree.setId(cctree.getId());
						}
						
						List<TreeNode> nodes = new ArrayList<TreeNode>();					
						ctree.getNodes().forEach(cnode -> {						
							if(cctree!=null) {
								TreeNode prevnode = treeService.getNodeRepo().findBySlugAndParent(cnode.getSlug(), cnode.getParent());
								if(prevnode!=null) {
									treeService.getNodeRepo().delete(prevnode);
								}							
							}						
							nodes.add(cnode);						
						});
						
						ctree.setNodes(new ArrayList<TreeNode>());
						
						treeService.getTreeRepo().save(ctree);
						treeService.getTreeRepo().flush();
						
						final Tree fctree = treeService.getTreeRepo().findByModuleAndSlug(ctree.getModule(), ctree.getSlug());
						nodes.forEach(cnode -> {						
							cnode = fixNode(cnode,fctree);
							TreeNode prevnode = treeService.getNodeRepo().findFirstByFullPathAndTree(cnode.getFullPath(), fctree);
							if(prevnode==null) {							
								TreeNode saved = treeService.getNodeRepo().save(cnode);
								cnode.getChildren().forEach(child->{
									child.setParent(saved);
									TreeNode savedchild = treeService.getNodeRepo().save(child);
								});
							}
							else {
								cnode.getChildren().forEach(child->{
									child.setParent(prevnode);
									TreeNode savedchild = treeService.getNodeRepo().save(child);
								});
							}
						});					
						treeService.getNodeRepo().flush();
					});
				}
			}
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
	}
	
	public void exportModule(String module) {
		ObjectMapper objectMapper = new ObjectMapper();
		DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
	            .withObjectIndenter(new DefaultIndenter("  ", "\n"));
		String cwd = new File("").getAbsolutePath() + "/custom_modules";
		String prepend = settingService.StringSetting("module_folder",cwd);
		String mod_path = prepend + "/" + module + "/";
		File mpf = new File(mod_path);
		if(!mpf.exists()) {
			mpf.mkdirs();
		}
		
		List<PortalPage> pages = pageService.getRepo().findAllByModule(module);
		try {			
			Path path = Paths.get(mod_path + "pages.json");
			byte[] data = objectMapper.writer(printer).writeValueAsBytes(pages);		
			Files.write(path, data);			
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
			Path path = Paths.get(mod_path + "files.json");
			byte[] data = objectMapper.writer(printer).writeValueAsBytes(files);		
			Files.write(path, data);
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
			Path path = Paths.get(mod_path + "trackers.json");
			byte[] data = objectMapper.writer(printer).writeValueAsBytes(trackers);		
			Files.write(path, data);
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
			Path path = Paths.get(mod_path + "settings.json");
			byte[] data = objectMapper.writer(printer).writeValueAsBytes(settings);		
			Files.write(path, data);
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
			Path path = Paths.get(mod_path + "trees.json");
			byte[] data = objectMapper.writer(printer).writeValueAsBytes(trees);		
			Files.write(path, data);
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
	
	public List<Module> list() {
		System.out.println("got here though");
		return repo.findAll();
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
		
		modules = jdbctemplate.queryForRowSet("select distinct module from " + FileLink.TABLE_NAME);
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
		modules = jdbctemplate.queryForRowSet("select distinct module from " + Setting.TABLE_NAME);
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
		
		String cwd = new File("").getAbsolutePath() + "/custom_modules";
		String prepend = settingService.StringSetting("module_folder",cwd);		
		
		File folder = new File(prepend);
		File[] listOfFiles = folder.listFiles(); 
		if(listOfFiles!=null){ 
			for(int i=0;i<listOfFiles.length;i++) {
				if(listOfFiles[i].isDirectory()) {				
					String dmod = listOfFiles[i].getName();
					if(!cmodules.contains(dmod)){
						cmodules.add(dmod);
					}
				}
			}		
		}
		
		jdbctemplate.execute("delete from portal_module");
		cmodules.forEach(cmod -> {
			Module curmod = new Module();
			curmod.setName(cmod);
			repo.save(curmod);
		});
	}
	
	public void copyModule(String module, String new_module) {
		ObjectMapper objectMapper = new ObjectMapper();
				
		List<PortalPage> pages = null;
		pages = pageService.getRepo().findAllByModule(module);
		if(pages.size()>0) {
			pages.forEach(page -> {					
				PortalPage curp = new PortalPage();
				curp.setAllowedRoles(page.getAllowedRoles());
				curp.setContent(page.getContent());
				curp.setCreatedBy(page.getCreatedBy());
				curp.setCreatedDate(page.getCreatedDate());
				curp.setModule(new_module);
				curp.setSlug(page.getSlug());
				curp.setPage_type(page.getPage_type());
				curp.setPageData(page.getPageData());
				curp.setPublished(page.getPublished());
				curp.setRequireLogin(page.getRequireLogin());
				curp.setRunable(page.getRunable());
				curp.setTitle(page.getTitle());
				pageService.getRepo().save(curp);					
			});
			pageService.getRepo().flush();
		}		
		
		List<Tracker> trackers = null;
		trackers = trackerService.getRepo().findAllByModule(module);
		 if(trackers.size()>0) {
			 trackers.forEach(ctracker -> {
				 Tracker newtracker = new Tracker();
				 
				 newtracker.setAddRoles(ctracker.getAddRoles());
				 newtracker.setDataTable(ctracker.getDataTable().replace(module + "_" + ctracker.getSlug(),new_module + "_" + ctracker.getSlug()));
				 newtracker.setDefaultStatus(ctracker.getDefaultStatus());
				 newtracker.setDeleteRoles(ctracker.getDeleteRoles());
				 newtracker.setDetailRoles(ctracker.getDetailRoles());
				 newtracker.setDisplayFields(ctracker.getDisplayFields());
				 newtracker.setEditRoles(ctracker.getEditRoles());
				 newtracker.setExcelFields(ctracker.getExcelFields());
				 newtracker.setFilterFields(ctracker.getFilterFields());
				 newtracker.setFormFields(ctracker.getFormFields());
				 newtracker.setInitialStatus(ctracker.getInitialStatus());
				 newtracker.setListFields(ctracker.getListFields());
				 newtracker.setModule(new_module);
				 newtracker.setName(ctracker.getName());
				 newtracker.setNodeId(ctracker.getNodeId());
				 newtracker.setPostCreate(ctracker.getPostCreate());
				 newtracker.setPostDataUpdate(ctracker.getPostDataUpdate());
				 newtracker.setPostDelete(ctracker.getPostDelete());
				 newtracker.setPostEdit(ctracker.getPostEdit());
				 newtracker.setSearchFields(ctracker.getSearchFields());
				 newtracker.setSlug(ctracker.getSlug());
				 newtracker.setTrackerType(ctracker.getTrackerType());
				 newtracker.setUpdatesTable(ctracker.getUpdatesTable().replace(module + "_" + ctracker.getSlug(),new_module + "_" + ctracker.getSlug()));
				 newtracker.setViewListRoles(ctracker.getViewListRoles());
				 trackerService.getRepo().save(newtracker);
				 trackerService.getRepo().flush();
				 
				 ctracker.getFields().forEach( cfield-> {
					 TrackerField newfield = new TrackerField();
					 newfield.setTracker(newtracker);
					 newfield.setAutoValue(cfield.getAutoValue());
					 newfield.setFieldType(cfield.getFieldType());
					 newfield.setFieldWidget(cfield.getFieldWidget());
					 newfield.setLabel(cfield.getLabel());
					 newfield.setName(cfield.getName());
					 newfield.setOptionSource(cfield.getOptionSource());
					 newfield.setOptionSourceGroovy(cfield.getOptionSourceGroovy());
					 newfield.setOptionSourceType(cfield.getOptionSourceType());
					 trackerService.getFieldRepo().save(newfield);
					 trackerService.getFieldRepo().flush();
				 });					 					 
			 });				 
		 }		
		
		List<Setting> settings = null;
		settings = settingService.getRepo().findAllByModule(module);
		if(settings.size()>0) {
			settings.forEach(csetting -> {
				
				Setting cc = new Setting();
				cc.setDateValue(csetting.getDateValue());
				cc.setModule(new_module);
				cc.setName(csetting.getName());
				cc.setNumberValue(csetting.getNumberValue());
				cc.setTextValue(csetting.getTextValue());
				cc.setType(csetting.getType());
				
				settingService.getRepo().save(cc);
				settingService.getRepo().flush();
			});				
		}
	}
}
