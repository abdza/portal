package org.portalengine.portal.Tracker;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.portalengine.portal.PoiExcel;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Page.PortalPage;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/trackers")
public class TrackerController {
	
		@Autowired
		private TrackerService service;
		
		@Autowired
		private FileLinkService fileservice;
		
		@Autowired
		private PageService pageService;
		
		private DataSource datasource;
		
		private JdbcTemplate jdbctemplate;
		
		
		@Autowired
		public TrackerController(DataSource datasource) {
			this.datasource = datasource;
			this.jdbctemplate = new JdbcTemplate(datasource);
		}

		@GetMapping
		public String list(HttpServletRequest request, Model model) {
			int page = 0;
			int size = 10;
			if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
				page = Integer.parseInt(request.getParameter("page"));
			}
			if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
				size = Integer.parseInt(request.getParameter("size"));
			}
			model.addAttribute("pageTitle","Tracker Listing");
			
			String search = "";
			Page<Tracker> toreturn = null;
			if(request.getParameter("q")!=null||(request.getParameter("module")!=null && !request.getParameter("module").equals("All"))) {
				System.out.println("doing query");
				String module = request.getParameter("module");
				search = "%" + request.getParameter("q").replace(" " , "%") + "%";
				Pageable pageable = PageRequest.of(page, size);
				if(module.equals("All")) {
					toreturn = service.getRepo().apiquery(search,pageable);
				}
				else {
					toreturn = service.getRepo().apimodulequery(search, module, pageable);
				}
			}
			else {
				toreturn = service.getRepo().findAll(PageRequest.of(page, size));
			}
			
			model.addAttribute("trackers", toreturn);
			
			//model.addAttribute("trackers", service.getRepo().findAll(PageRequest.of(page, size)));
			return "tracker/list.html";
		}
		
		@GetMapping(value= {"/create","/edit/{id}"})
		public String form(@PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				Tracker tracker = service.getRepo().getOne(id);
				model.addAttribute("pageTitle","Edit Tracker - " + tracker.getName());
				model.addAttribute("tracker", tracker);
				model.addAttribute("postCreate",pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(),tracker.getPostCreate()));
				model.addAttribute("postEdit",pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(),tracker.getPostEdit()));
				model.addAttribute("postDelete",pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(),tracker.getPostDelete()));
				model.addAttribute("postDataUpdate",pageService.getRepo().findOneByModuleAndSlug(tracker.getModule(),tracker.getPostDataUpdate()));
			}
			else {
				model.addAttribute("pageTitle","Create Tracker");
				model.addAttribute("tracker",new Tracker());
			}
			return "tracker/form.html";
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.deleteById(id);
			return "redirect:/admin/trackers";
		}
		
		@GetMapping("/updatedb/{id}")
		public String updateDb(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			service.updateDb(tracker);
			return "redirect:/admin/trackers/display/" + id.toString();
		}
		
		@PostMapping("/cleardb/{id}")
		public String clearDb(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			service.clearDb(tracker);
			return "redirect:/admin/trackers/display/" + id.toString();
		}
		
		@GetMapping("/fixstatus/{id}")
		public String fixstatus(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			service.FixStatus(tracker);
			return "redirect:/admin/trackers/display/" + id.toString();
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			model.addAttribute("pageTitle","Tracker - " + tracker.getName());
			model.addAttribute("tracker", tracker);
			return "tracker/display.html";
		}
		
		@GetMapping("/{id}/exceltemplate")
		public String exceltemplate(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			model.addAttribute("pageTitle","Tracker Template - " + tracker.getName());
			model.addAttribute("tracker", tracker);
			return "tracker/exceltemplate.html";
		}
		
		@PostMapping("/{id}/savetemplate")
		public String saveexceltemplate(@PathVariable Long id, Model model, HttpServletRequest request) {
			Tracker tracker = service.getRepo().getOne(id);
			Map<String, String[]> postdata = request.getParameterMap();
			
			for(Map.Entry<String, String[]> pdata:postdata.entrySet()) {
				System.out.println("Key:" + pdata.getKey());
				System.out.println("Value:" + pdata.getValue().toString());
				if(pdata.getKey().length()>5 && pdata.getKey().substring(0, 4).equals("col_")) {
					String fkey = pdata.getKey().substring(4);
					TrackerField nfield = new TrackerField();
					nfield.setName(fkey);
					nfield.setLabel(postdata.get("lbl_" + fkey)[0]);
					nfield.setFieldType(pdata.getValue()[0]);
					nfield.setFieldWidget("Default");
					nfield.setTracker(tracker);
					tracker.add(nfield);
					service.getFieldRepo().save(nfield);
				}
			}
			return "redirect:/admin/trackers/fields/" + id.toString();
		}
		
		@PostMapping("/{id}/exceltemplate")
		public String submitexceltemplate(@RequestParam Map<String,String> postdata, @RequestParam("file") MultipartFile file, @PathVariable Long id, Model model, HttpServletRequest request) {
			Tracker tracker = service.getRepo().getOne(id);
			model.addAttribute("tracker", tracker);
			model.addAttribute("trackerfield",new TrackerField());
			HashMap<String, String> fieldname = new HashMap<String, String>();
			HashMap<String, String> fieldtype = new HashMap<String, String>();
			String tmpfilepath = fileservice.SaveTmpFile(file);
			System.out.println("File saved to:" + tmpfilepath);
			PoiExcel poiExcel = new PoiExcel();
			poiExcel.setLimits(Integer.parseInt(postdata.get("headerstart")), Integer.parseInt(postdata.get("headerend")), Integer.parseInt(postdata.get("headerend"))+1);
			try {
				List<Object> fields = poiExcel.getHeaders(tmpfilepath);
				//HashMap<String, String> field = new HashMap<String, String>();
				for(Object field: fields) {
					HashMap<String, String> cfield = (HashMap<String, String>)field;
					fieldname.put(cfield.get("name"), cfield.get("text"));
					fieldtype.put(cfield.get("name"), cfield.get("type"));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			model.addAttribute("fieldname",fieldname);
			model.addAttribute("fieldtype",fieldtype);
			
			return "tracker/exceltype.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Tracker tracker,Model model) {
			service.getRepo().save(tracker);
			return "redirect:/admin/trackers/display/" + tracker.getId().toString();
		}
		
}
