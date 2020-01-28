package org.portalengine.portal.Tracker;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/trackers")
public class TrackerController {
	
		@Autowired
		private TrackerService service;
		
		@Autowired
		private FileLinkService fileservice;
		
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
			model.addAttribute("trackers", service.getRepo().findAll(PageRequest.of(page, size)));
			return "tracker/list.html";
		}
		
		@GetMapping("/create")
		public String create(Model model) {
			model.addAttribute("tracker", new Tracker());
			return "tracker/form.html";
		}
		
		@GetMapping("/edit/{id}")
		public String edit(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			model.addAttribute("tracker", tracker);
			return "tracker/form.html";
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.getRepo().deleteById(id);
			return "redirect:/trackers";
		}
		
		@GetMapping("/updatedb/{id}")
		public String updateDb(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			tracker.updateDb(jdbctemplate);
			return "redirect:/trackers/display/" + id.toString();
		}
		
		@GetMapping("/fixstatus/{id}")
		public String fixstatus(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			service.FixStatus(tracker);
			return "redirect:/trackers/display/" + id.toString();
		}
		
		@GetMapping("/display/{id}")
		public String display(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
			model.addAttribute("tracker", tracker);
			return "tracker/display.html";
		}
		
		@GetMapping("/{id}/exceltemplate")
		public String exceltemplate(@PathVariable Long id, Model model) {
			Tracker tracker = service.getRepo().getOne(id);
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
			return "redirect:/trackers/fields/" + id.toString();
		}
		
		@PostMapping("/{id}/exceltemplate")
		public String submitexceltemplate(@RequestParam("file") MultipartFile file, @PathVariable Long id, Model model, HttpServletRequest request) {
			Tracker tracker = service.getRepo().getOne(id);
			model.addAttribute("tracker", tracker);
			model.addAttribute("trackerfield",new TrackerField());
			Workbook workbook;
			Map<String, String[]> postdata = request.getParameterMap();
			HashMap<String, String> fieldname = new HashMap<String, String>();
			HashMap<String, String> fieldtype = new HashMap<String, String>();
			try {
				String tmpfilepath = fileservice.SaveTmpFile(file);
				System.out.println("File saved to:" + tmpfilepath);
				File tmpexcel = new File(tmpfilepath);
				workbook = WorkbookFactory.create(tmpexcel);
				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rows = sheet.rowIterator();
				Row drow;
				Row datarow;
				Boolean stoprow=false;
				while(rows.hasNext() && !stoprow) {
					System.out.println("Looping over excel " + postdata.get("headerend")[0].toString() + " until " + postdata.get("headerstart")[0].toString());
					drow = rows.next();
					System.out.println("Current row:" + String.valueOf(drow.getRowNum()));
					datarow = sheet.getRow(drow.getRowNum()+1);
					if(drow.getRowNum()<Integer.parseInt(postdata.get("headerend")[0])) {
						if(drow.getRowNum()>=Integer.parseInt(postdata.get("headerstart")[0])-1) {
							Iterator<Cell> cells = drow.cellIterator();					
							Cell cell;
							while(cells.hasNext()) {
								cell = cells.next();
								String cellval = cell.getStringCellValue();
								System.out.println("Name contents:" + cell.getStringCellValue());
								String cellkey = cellval.replace(" ", "_").replaceAll("[^A-Za-z0-9_]","").toLowerCase();
								fieldname.put(cellkey, cellval);
								Cell datacell = datarow.getCell(cell.getColumnIndex());
								if(datacell == null) {
									fieldtype.put(cellkey, "String");
								}
								else {
									if(datacell.getCellType()==CellType.STRING) {
										fieldtype.put(cellkey, "String");
									}
									else if(datacell.getCellType()==CellType.NUMERIC) {
										fieldtype.put(cellkey, "Integer");
									}
								}
							}
						}
					}
					else {
						stoprow = true;
					}
				}
				model.addAttribute("fieldname",fieldname);
				model.addAttribute("fieldtype",fieldtype);
				workbook.close();
				tmpexcel.delete();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return "tracker/exceltype.html";
		}
		
		@PostMapping("/save")
		public String save(@Valid Tracker tracker,Model model) {
			service.getRepo().save(tracker);
			return "redirect:/trackers/display/" + tracker.getId().toString();
		}
		
}
