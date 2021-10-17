package org.portalengine.portal.Tracker;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.PortalPage;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.portalengine.portal.Tree.TreeService;
import org.portalengine.portal.User.User;
import org.portalengine.portal.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Controller
@RequestMapping("/")
public class SystemController {
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TreeService treeService;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	/* Read application.properties with the following function:
	 * String keyValue = env.getProperty(key);
	 */
	@Autowired
	private Environment env;
	
	@Autowired
	public SystemController() {
	}
	
	@GetMapping("/{module}/{slug}/transition/{transition_id}/{data_id}")
	public String createdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long transition_id, @PathVariable Long data_id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			TrackerTransition transition = trackerService.getTransitionRepo().getOne(transition_id);
			model.addAttribute("trackerservice",trackerService);
			model.addAttribute("tracker", tracker);
			model.addAttribute("transition",transition);
			String formtitle = tracker.getName() + " " + transition.getName();
			model.addAttribute("formtitle",formtitle);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, data_id);
			model.addAttribute("datas", datarow);
			PortalPage pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_" + trackerService.slugify(transition.getName()));
			if(pp!=null) {
				
				Object pdata = null;
				if(pp.getPageData()!=null && pp.getPageData().length()>0) {
					Binding binding = new Binding();		
					GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
					Map<String, String[]> postdata = request.getParameterMap();
					binding.setVariable("pageService",pageService);
					binding.setVariable("postdata", postdata);
					binding.setVariable("request", request);
					binding.setVariable("trackerService",trackerService);
					binding.setVariable("treeService",treeService);
					binding.setVariable("userService",userService);
					binding.setVariable("fileService",fileService);
					binding.setVariable("namedjdbctemplate", namedjdbctemplate);
					binding.setVariable("env", env);
					try {
						pdata = shell.evaluate(pp.getPageData());
					}
					catch(Exception e) {
						System.out.println("Error in page:" + e.toString());
					}
				}
				model.addAttribute("pdata",pdata);
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/create")
	@PreAuthorize("trackerPermission(#module,#slug,'add')")
	public String createdata(@PathVariable String module, @PathVariable String slug, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("trackerservice",trackerService);
			model.addAttribute("tracker", tracker);
			String formtitle = "New " + tracker.getName();
			model.addAttribute("formtitle",formtitle);			
			model.addAttribute("pageTitle",formtitle);
			model.addAttribute("transition",trackerService.create_transition(tracker));
			PortalPage pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_create");
			if(pp!=null) {
				Object pdata = null;
				if(pp.getPageData()!=null && pp.getPageData().length()>0) {
					Binding binding = new Binding();		
					GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
					Map<String, String[]> postdata = request.getParameterMap();
					binding.setVariable("pageService",pageService);
					binding.setVariable("postdata", postdata);
					binding.setVariable("request", request);
					binding.setVariable("trackerService",trackerService);
					binding.setVariable("treeService",treeService);
					binding.setVariable("userService",userService);
					binding.setVariable("fileService",fileService);
					binding.setVariable("namedjdbctemplate", namedjdbctemplate);
					binding.setVariable("env", env);
					try {
						pdata = shell.evaluate(pp.getPageData());
					}
					catch(Exception e) {
						System.out.println("Error in page:" + e.toString());
					}
				}
				model.addAttribute("pdata",pdata);
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@GetMapping("/{module}/{slug}/display/{id}")
	@PreAuthorize("trackerPermission(#module,#slug,'detail')")
	public String displaydata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("tracker", tracker);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			String datatitle = tracker.getName() + " - Details";
			System.out.println("Title:" + datatitle);
			model.addAttribute("datatitle",datatitle);
			model.addAttribute("pageTitle",datatitle);
			PortalPage pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_display");
			if(pp!=null) {
				Object pdata = null;
				if(pp.getPageData()!=null && pp.getPageData().length()>0) {
					Binding binding = new Binding();		
					GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
					Map<String, String[]> postdata = request.getParameterMap();
					binding.setVariable("pageService",pageService);
					binding.setVariable("postdata", postdata);
					binding.setVariable("request", request);
					binding.setVariable("trackerService",trackerService);
					binding.setVariable("treeService",treeService);
					binding.setVariable("userService",userService);
					binding.setVariable("fileService",fileService);
					binding.setVariable("namedjdbctemplate", namedjdbctemplate);
					binding.setVariable("env", env);
					try {
						pdata = shell.evaluate(pp.getPageData());
					}
					catch(Exception e) {
						System.out.println("Error in page:" + e.toString());
					}
				}
				model.addAttribute("pdata",pdata);
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			return "tracker/data/display.html";
		}
		else {
			return "404";
		}
	}
	
	@PostMapping("/{module}/{slug}/delete/{id}")
	@PreAuthorize("trackerPermission(#module,#slug,'delete')")
	public String deletedata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		paramsource.addValue("id", id);
		namedjdbctemplate.update("delete from " + tracker.getDataTable() + " where " + trackerService.dbEscapeColumn("id") + "=:id", paramsource);
		PortalPage postpage = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getPostDelete());
		if(postpage!=null) {
			return "redirect:/view/" + tracker.getModule() + "/" + postpage.getSlug();
		}
		else {
			return "redirect:/" + tracker.getModule() + "/" + tracker.getSlug() + "/list";
		}		
	}
	
	@GetMapping("/{module}/{slug}/edit/{id}")
	@PreAuthorize("trackerPermission(#module,#slug,'edit')")
	public String editdata(@PathVariable String module, @PathVariable String slug, @PathVariable Long id, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			model.addAttribute("tracker", tracker);
			String formtitle = "Edit " + tracker.getName();
			model.addAttribute("formtitle",formtitle);
			model.addAttribute("pageTitle",formtitle);
			HashMap<String,Object> datarow = trackerService.datarow(tracker, id);
			model.addAttribute("datas", datarow);
			PortalPage pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_edit");
			if(pp!=null) {
				Object pdata = null;
				if(pp.getPageData()!=null && pp.getPageData().length()>0) {
					Binding binding = new Binding();		
					GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
					Map<String, String[]> postdata = request.getParameterMap();
					binding.setVariable("pageService",pageService);
					binding.setVariable("postdata", postdata);
					binding.setVariable("request", request);
					binding.setVariable("trackerService",trackerService);
					binding.setVariable("treeService",treeService);
					binding.setVariable("userService",userService);
					binding.setVariable("fileService",fileService);
					binding.setVariable("namedjdbctemplate", namedjdbctemplate);
					binding.setVariable("env", env);
					try {
						pdata = shell.evaluate(pp.getPageData());
					}
					catch(Exception e) {
						System.out.println("Error in page:" + e.toString());
					}
				}
				model.addAttribute("pdata",pdata);
				model.addAttribute("page",pp);
				model.addAttribute("content", pp.getContent());				
				return "page/plain.html";
			}
			return "tracker/data/form.html";
		}
		else {
			return "404";
		}
	}
	
	@PostMapping("/{module}/{slug}/save")
	public String save(@PathVariable String module, @PathVariable String slug, Model model,Authentication authentication) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		if(tracker!=null) {
			User curuser = null;
			if(authentication!=null) {
				curuser = (User)authentication.getPrincipal();
			}
			
			Map<String, String[]> postdata = request.getParameterMap();
			Long curid = null;
			if(!postdata.containsKey("cancel")) {				
				curid = trackerService.saveForm(tracker,curuser);
			}
			
			if(postdata.get("transition_id")!=null) {
				TrackerTransition transition = trackerService.getTransitionRepo().getOne(Long.parseLong(postdata.get("transition_id")[0]));
			}
			if(postdata.get("id")!=null) {				
				PortalPage postpage = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getPostEdit());
				if(postpage!=null) {
					return "redirect:/view/" + tracker.getModule() + "/" + postpage.getSlug() + "/" + postdata.get("id")[0].toString();
				}
				else {
					return "redirect:/" + tracker.getModule() + "/" + tracker.getSlug() + "/display/" + postdata.get("id")[0].toString();
				}
			}
			else {			
				PortalPage postpage = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getPostCreate());
				if(postpage!=null) {
					return "redirect:/view/" + tracker.getModule() + "/" + postpage.getSlug() + "/" + String.valueOf(curid);
				}
				else {
					return "redirect:/" + tracker.getModule() + "/" + tracker.getSlug() + "/list";
				}
			}
		} 
		else {
			return "404";
		}
	}
		
	@GetMapping("/{module}/{slug}/list")
	@PreAuthorize("trackerPermission(#module,#slug,'list')")
	public String list(@PathVariable String module, @PathVariable String slug, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		model.addAttribute("tracker", tracker);
		String listtitle = tracker.getName();
		DataSet dataset = trackerService.dataset(tracker);
		model.addAttribute("dataset",dataset);
		model.addAttribute("listtitle",listtitle);
		model.addAttribute("pageTitle",listtitle);
		PortalPage pp = pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(), tracker.getSlug() + "_list");
		if(pp!=null) {
			Object pdata = null;
			if(pp.getPageData()!=null && pp.getPageData().length()>0) {
				Binding binding = new Binding();		
				GroovyShell shell = new GroovyShell(getClass().getClassLoader(),binding);
				Map<String, String[]> postdata = request.getParameterMap();
				binding.setVariable("pageService",pageService);
				binding.setVariable("postdata", postdata);
				binding.setVariable("request", request);
				binding.setVariable("trackerService",trackerService);
				binding.setVariable("treeService",treeService);
				binding.setVariable("userService",userService);
				binding.setVariable("fileService",fileService);
				binding.setVariable("namedjdbctemplate", namedjdbctemplate);
				binding.setVariable("env", env);
				try {
					pdata = shell.evaluate(pp.getPageData());
				}
				catch(Exception e) {
					System.out.println("Error in page:" + e.toString());
				}
			}
			model.addAttribute("pdata",pdata);
			model.addAttribute("page",pp);
			model.addAttribute("content", pp.getContent());				
			return "page/plain.html";
		}
		return "tracker/data/list.html";
	}
	
	@GetMapping("/{module}/{slug}/excel")
	@PreAuthorize("trackerPermission(#module,#slug,'list')")
	@ResponseBody
	public ResponseEntity<StreamingResponseBody> excel(@PathVariable String module, @PathVariable String slug, Model model) {
		Tracker tracker = trackerService.getRepo().findOneByModuleAndSlug(module, slug);
		model.addAttribute("tracker", tracker);
		String listtitle = tracker.getName();
		DataSet dataset = trackerService.dataset(tracker,false);
		String exceltitle = tracker.getName().toLowerCase().replaceAll(" ", "_") + ".xlsx";
		
		SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
		Sheet sheet = wb.createSheet();		
		
		Row headerRow = sheet.createRow(0);
		List<TrackerField> flist = trackerService.field_list(tracker,"excel",null);
		for(int i=0;i<flist.size();i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(flist.get(i).getLabel());
		}
		
		for(int j=0;j<dataset.getDataRows().length;j++) {
			Row currow = sheet.createRow(j+1);
			HashMap<String,Object> datarow = (HashMap<String, Object>) dataset.getDataRows()[j];
			for(int i=0;i<flist.size();i++) {
				TrackerField curfield = flist.get(i);
				Cell cell = currow.createCell(i);
				switch(curfield.getFieldType()) {
				case "String":
				case "Text":
				case "TrackerType":
				case "TreeNode":				
				case "User":
				case "Integer":
				case "Number":
				case "Date":
				case "DateTime":
					String datedata = trackerService.display(curfield, datarow);
					cell.setCellValue(datedata);
					break;
				}
			}	
		}
		
		return ResponseEntity
			    .ok()
			    .contentType(MediaType.APPLICATION_OCTET_STREAM)
			    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"" + exceltitle + "\"")
			    .body(wb::write);
	}
}
