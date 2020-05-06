package org.portalengine.portal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import java.sql.Timestamp;
import java.lang.NumberFormatException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;

public class PoiExcel {
		
        enum xssfDataType {
            BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
        }

	private XMLReader parser;
	private List<Object> fields = new ArrayList<Object>();
	private ReadOnlySharedStringsTable sst;
	private StylesTable stylesTable;
	private DataFormatter formatter;
	private int rowcount;
	public int headerstart=1;
	public int headerend=1;
	public int datastart=2;
	public int dataend=-1;
	private List<CellRangeAddress> mergedRegions;
	private HashMap<Integer, Object> titles;

	public PoiExcel() {
	}
	
	public void setLimits(int headerstart, int headerend, int datastart) {
		this.headerstart = headerstart;
		this.headerend = headerend;
		this.datastart = datastart;
	}

	public List<Object> getHeaders(String filename) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader( pkg );
		sst = new ReadOnlySharedStringsTable(pkg);
		stylesTable = r.getStylesTable();

		InputStream sheet = r.getSheetsData().next();
		InputSource regionSource = new InputSource(sheet);

		MergedRegionLocator mergedRegionLocation = new MergedRegionLocator();
		XMLReader regionParser = XMLHelper.newXMLReader();
		regionParser.setContentHandler(mergedRegionLocation);
		try {
			regionParser.parse(regionSource); 
			mergedRegions = mergedRegionLocation.getMergedRegions();
		}
		catch(SAXException e){
			System.out.println("Region exception:" + e.toString());
		}
		sheet.close();
		

		XSSFReader sr = new XSSFReader( pkg );
		SheetHeaderHandler headerHandler = new SheetHeaderHandler();
		XMLReader sheetparser = XMLHelper.newXMLReader();
		sheetparser.setContentHandler(headerHandler);
		InputStream sstream = sr.getSheetsData().next();
		InputSource sheetSource = new InputSource(sstream);

		if(sheetSource!=null) {
			try {
				sheetparser.parse(sheetSource);
			}
			catch(SAXException e){
				System.out.println("Exception:" + e.toString());
			}
			titles = headerHandler.getTitles();
			for(Map.Entry<Integer,Object> title:titles.entrySet()){
				fields.add(title.getValue());
			}
		}
		sstream.close();

		pkg.close();
		return fields;
	}

	public class MergedRegionLocator extends DefaultHandler {
	    private final List<CellRangeAddress> mergedRegions = new ArrayList<CellRangeAddress>();

	    @Override
	    public void startElement (String uri, String localName, String name, Attributes attributes) {
		if ("mergeCell".equals(name) && attributes.getValue("ref") != null) {
		    mergedRegions.add(CellRangeAddress.valueOf(attributes.getValue("ref")));
		}
	    }

	    public CellRangeAddress getMergedRegion (int index) {
		return mergedRegions.get(index);
	    }

	    public List<CellRangeAddress> getMergedRegions () {
		return mergedRegions;
	    }
	}

	private class SheetHeaderHandler extends DefaultHandler {
		private String lastContents;
		private boolean nextIsString;
		private String dquery="";
		private boolean firstRow = true;
		private xssfDataType nextDataType;
		private CellAddress cellAddress;

		HashMap<String, String> field = new HashMap<String, String>();
		HashMap<Integer, Object> titles = new HashMap<Integer, Object>();

		// Used to format numeric cell values.
		private short formatIndex;
		private String formatString;

		private SheetHeaderHandler() {
			this.nextDataType = xssfDataType.NUMBER;
			formatter = new DataFormatter();
			fields = new ArrayList<Object>();
			rowcount = 0;
		}

		public HashMap<Integer,Object> getTitles() {
			return titles;
		}

	        @Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			// to get the coordinates System.out.println("R:" + attributes.getValue("r"));
			// c => cell
			if(name.equals("c")) {
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				String cellpos = attributes.getValue("r");
				cellAddress = new CellAddress(cellpos);
				if(cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
				// Set up defaults.
				this.nextDataType = xssfDataType.NUMBER;
				this.formatIndex = -1;
				this.formatString = null;
				String cellStyleStr = attributes.getValue("s");
				if ("b".equals(cellType))
					nextDataType = xssfDataType.BOOL;
				else if ("e".equals(cellType))
					nextDataType = xssfDataType.ERROR;
				else if ("inlineStr".equals(cellType))
					nextDataType = xssfDataType.INLINESTR;
				else if ("s".equals(cellType))
					nextDataType = xssfDataType.SSTINDEX;
				else if ("str".equals(cellType))
					nextDataType = xssfDataType.FORMULA;
				else if (cellStyleStr != null) {
					// It's a number, but almost certainly one
					// with a special style or format
					int styleIndex = Integer.parseInt(cellStyleStr);
					XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
					this.formatIndex = style.getDataFormat();
					this.formatString = style.getDataFormatString();
					if (this.formatString == null)
					    this.formatString = BuiltinFormats
						    .getBuiltinFormat(this.formatIndex);
				}
				
			}
			// Clear contents cache
			lastContents = "";
		}

		public void endElement(String uri, String localName, String name)
				throws SAXException {
				// Process the last contents as required.
			// Do now, as characters() may be called more than once
				if(nextIsString) {
					int idx = Integer.parseInt(lastContents);
					lastContents = sst.getItemAt(idx).getString();
					nextIsString = false;
				}
				// v => contents of a cell
				// Output after we've seen the string contents
				if(name.equals("v")) {
					if(cellAddress.getRow()+1>=headerstart && cellAddress.getRow()+1<=headerend) {
						String curname = lastContents.toLowerCase().replaceAll(" ","_").replaceAll("[^a-zA-Z0-9_]","").replaceAll("__","_");
						String curtext = lastContents;
						field = (HashMap<String,String>)titles.get(cellAddress.getColumn());
						if(field==null){
							field = new HashMap<String, String>();
						}
						else{
							curname = field.get("name") + "_" + curname;
							curtext = field.get("text") + " " + curtext;
						}
						field.put("name",curname);
						field.put("text",curtext);
						field.put("col",String.valueOf(cellAddress.getColumn()));
						field.put("type","String");
						titles.put(cellAddress.getColumn(),field);

						for(CellRangeAddress testrange:mergedRegions){
							if(testrange.isInRange(cellAddress.getRow(),cellAddress.getColumn())){
								for(int i=testrange.getFirstColumn()+1;i<=testrange.getLastColumn();i++){
									field = (HashMap<String,String>)titles.get(i);
									if(field==null){
										field = new HashMap<String, String>();
									}
									else{
										curname = field.get("name") + "_" + curname;
										curtext = field.get("text") + " " + curtext;
									}
									field.put("name",curname);
									field.put("text",curtext);
									field.put("col",String.valueOf(i));
									field.put("type","String");
									titles.put(i,field);
								}
							}
						}


					}
					else if(cellAddress.getRow()+1>=headerend) {
						// Check the row after the header to determine the proper type for the column

						field = (HashMap<String, String>)titles.get(cellAddress.getColumn());
						if(field!=null){
							if(this.nextDataType==xssfDataType.NUMBER){
								field.put("type","Number");
							}
							titles.put(cellAddress.getColumn(),field);
						}
					}
				}
				if(name.equals("row")) {
					rowcount += 1;
					if(cellAddress.getRow()+1>headerend+1){
						throw new SAXException("Done reading header and sample");
					}
				}
		}
		public void characters(char[] ch, int start, int length) {
			lastContents += new String(ch, start, length);
		}
	}

	/* public int loadData(String filename,Sql sql,JSONObject savedparams,List<Object> statementfields, String filtertable, Integer batchno, boolean gotupdate) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader( pkg );
		sst = new ReadOnlySharedStringsTable(pkg);
		fetchSheetParser(sql,savedparams,statementfields,filtertable,batchno,gotupdate);
		InputStream sheet = r.getSheetsData().next();
		InputSource sheetSource = new InputSource(sheet);
		if(sheetSource!=null) {
			try {
				this.parser.parse(sheetSource);
			}
			catch(SAXException e){
				System.out.println("Exception:" + e.toString());
			}
		}
		sheet.close();
		pkg.close();
		return rowcount;
	}

	private void fetchSheetParser(Sql sql, JSONObject savedparams,List<Object> statementfields,String filtertable,Integer batchno,boolean gotupdate) throws SAXException, ParserConfigurationException {
		this.parser = XMLHelper.newXMLReader();
		ContentHandler handler = new SheetHandler(sql,savedparams,statementfields,filtertable,batchno,gotupdate);
		this.parser.setContentHandler(handler);
	}

	private class SheetHandler extends DefaultHandler {
		private String lastContents;
		private boolean nextIsString;
		private String dquery="";
		private boolean firstRow = true;
		private Sql sql;
		private JSONObject savedparams;
		private String filtertable;
		private Integer batchno;
		private boolean gotupdate;
		private CellAddress cellAddress;

		List<Object> statementfields;
		HashMap<String, Object> qparam = new HashMap<String, Object>();
		HashMap<Integer, Object> currow;

		private SheetHandler(Sql sql, JSONObject savedparams, List<Object> statementfields, String filtertable, Integer batchno, boolean gotupdate) {
			this.sql = sql;
			this.savedparams = savedparams;
			this.statementfields = statementfields;
			this.filtertable = filtertable;
			this.batchno = batchno;
			this.gotupdate = gotupdate;
			rowcount = 0;
		}
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			// to get the coordinates System.out.println("R:" + attributes.getValue("r"));
			// c => cell
			if(name.equals("c")) {
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				String cellpos = attributes.getValue("r");
				cellAddress = new CellAddress(cellpos);
				if(cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
			}
			if(name.equals("row")) {
				currow = new HashMap<Integer, Object>();
				qparam = new HashMap<String, Object>();
			}
			// Clear contents cache
			lastContents = "";
		}
		public void endElement(String uri, String localName, String name)
				throws SAXException {
				// Process the last contents as required.
			// Do now, as characters() may be called more than once
				if(nextIsString) {
					int idx = Integer.parseInt(lastContents);
					lastContents = sst.getItemAt(idx).getString();
					nextIsString = false;
				}
				// v => contents of a cell
				// Output after we've seen the string contents
				if(name.equals("v")) {
					if(cellAddress.getRow()+1>=datastart && (dataend<0 || cellAddress.getRow()+1<=dataend)){
						currow.put(cellAddress.getColumn(),lastContents);
					}
				}
				if(name.equals("row")) {
					if(cellAddress.getRow()+1>=datastart && (dataend<0 || cellAddress.getRow()+1<=dataend)){
						String sqlfields="";
						String paramsqlfields="";
						String updatefields="";
						String comparefields="";
						boolean firstone = true;
						boolean firstcompare = true;
						boolean nodata = true;
						String torun = "select 1";
						for(Object field:statementfields) {
							HashMap<String,Object> cfield = (HashMap<String,Object>) field;

							String datasource = this.savedparams.optString("datasource_" + String.valueOf(cfield.get("id")));

							Object curdata;

							if(datasource.equals("custom")){
								curdata = this.savedparams.getString("custom_" + String.valueOf(cfield.get("id")));
							}
							else{
								Integer curpos = Integer.parseInt(datasource);
								curdata = currow.get(Integer.parseInt(datasource));
								if(curdata!=null){
									nodata = false;
								}
							}

							if(curdata!=null){
								if(!firstone){
									sqlfields += " , ";
									paramsqlfields += " , ";
									updatefields += " , ";
								}
								sqlfields += cfield.get("name");
								paramsqlfields += ":" + cfield.get("name");
								updatefields += cfield.get("name") + "=:" + cfield.get("name");

								String updatecheck = this.savedparams.optString("update_" + String.valueOf(cfield.get("id")));
								if(updatecheck!=""){
									if(!firstcompare){
										comparefields += " and ";
									}
									comparefields += cfield.get("name") + "=:" + cfield.get("name");
									firstcompare = false;
								}
							
								try {
									if(cfield.get("type").equals("Date")){
										try {
											Timestamp curval = new Timestamp(DateUtil.getJavaDate(Double.parseDouble((String)curdata)).getTime());
											qparam.put((String)cfield.get("name"),curval);
										}
										catch(NumberFormatException e){
											String datedata = curdata.toString();
											if(curdata.toString().length()<=10){
												if(datedata.indexOf('/')>0){
													String[] dateparts = datedata.split("/");
													datedata = dateparts[2] + "-" + dateparts[1] + "-" + dateparts[0];
												}
												Date curval = Date.valueOf(datedata);
												qparam.put((String)cfield.get("name"),curval);
											}
											else{
												if(datedata.indexOf('/')>0){
													String[] dateparts = datedata.split("/");
													datedata = dateparts[2] + "-" + dateparts[1] + "-" + dateparts[0];
												}
												Timestamp curval = Timestamp.valueOf(datedata);
												qparam.put((String)cfield.get("name"),curval);
											}
										}
									}
									else if(cfield.get("type").equals("Number")){
										Double curval = Double.parseDouble((String)curdata);
										qparam.put((String)cfield.get("name"),curval);
									}
									else if(cfield.get("type").equals("Bulat")){
										Integer curval = Integer.parseInt((String)curdata);
										qparam.put((String)cfield.get("name"),curval);
									}
									else{
										String curval = (String)curdata;
										qparam.put((String)cfield.get("name"),curval);
									}
								}
								catch(Exception e){
									System.out.println("Error converting:" + e.toString());
								}
								firstone = false;
							}
						}
						try {
							if(!nodata){
								qparam.put("batchno",batchno);
								if(!gotupdate){
									torun = "insert into " + this.filtertable + " (" + sqlfields + " , batchno) values (" + paramsqlfields + " , :batchno)";
									sql.executeUpdate(qparam, torun);
								}
								else{
									updatefields += " , batchno=:batchno";
									torun = "update " + this.filtertable + " set " + updatefields + " where " + comparefields;
									int updated = 0;
									updated = sql.executeUpdate(qparam, torun);
									if(updated==0){
										torun = "insert into " + this.filtertable + " (" + sqlfields + " , batchno) values (" + paramsqlfields + " , :batchno)";
										sql.executeUpdate(qparam, torun);
									}
								}
							}
						}
						catch(SQLException e){
							System.out.println("SQL exception found:" + e.toString());
							System.out.println("torun:" + torun);
							System.out.println("qparam:" + qparam.toString());
						}
					}
					firstRow = false;
					rowcount += 1;
				}
		}
		public void characters(char[] ch, int start, int length) {
			lastContents += new String(ch, start, length);
		}
	}

	public HashMap<String, Object> findHeaders(String filename,List<Object> statementfields) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader( pkg );
		sst = new ReadOnlySharedStringsTable(pkg);
		stylesTable = r.getStylesTable();

		XSSFReader sr = new XSSFReader( pkg );
		FindHeaderHandler findHeaderHandler = new FindHeaderHandler(statementfields);
		XMLReader sheetparser = XMLHelper.newXMLReader();
		sheetparser.setContentHandler(findHeaderHandler);
		InputStream sstream = sr.getSheetsData().next();
		InputSource sheetSource = new InputSource(sstream);
		HashMap<String, Object> foundHeaders = new HashMap<String, Object>();

		if(sheetSource!=null) {
			try {
				sheetparser.parse(sheetSource);
			}
			catch(SAXException e){
				System.out.println("Exception:" + e.toString());
			}
			titles = findHeaderHandler.getTitles();
			for(Map.Entry<Integer,Object> title:titles.entrySet()){
				HashMap<String, String> field = (HashMap<String, String>)title.getValue();
				if(field.get("name").length()>1){
					foundHeaders.put(field.get("name"),field);
				}
			}
		}
		sstream.close();

		pkg.close();
		return foundHeaders;
	}

	private class FindHeaderHandler extends DefaultHandler {
		private String lastContents;
		private boolean nextIsString;
		private String dquery="";
		private boolean firstRow = true;
		private xssfDataType nextDataType;
		private CellAddress cellAddress;
		List<Object> statementfields;

		HashMap<String, String> field = new HashMap<String, String>();
		HashMap<Integer, Object> titles = new HashMap<Integer, Object>();

		// Used to format numeric cell values.
		private short formatIndex;
		private String formatString;

		private FindHeaderHandler(List<Object> statementfields) {
			this.nextDataType = xssfDataType.NUMBER;
			formatter = new DataFormatter();
			fields = new ArrayList<Object>();
			rowcount = 0;
			this.statementfields = statementfields;
		}

		public HashMap<Integer,Object> getTitles() {
			return titles;
		}

	        @Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			// to get the coordinates System.out.println("R:" + attributes.getValue("r"));
			// c => cell
			if(name.equals("c")) {
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				String cellpos = attributes.getValue("r");
				cellAddress = new CellAddress(cellpos);
				if(cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
				// Set up defaults.
				this.nextDataType = xssfDataType.NUMBER;
				this.formatIndex = -1;
				this.formatString = null;
				String cellStyleStr = attributes.getValue("s");
				if ("b".equals(cellType))
					nextDataType = xssfDataType.BOOL;
				else if ("e".equals(cellType))
					nextDataType = xssfDataType.ERROR;
				else if ("inlineStr".equals(cellType))
					nextDataType = xssfDataType.INLINESTR;
				else if ("s".equals(cellType))
					nextDataType = xssfDataType.SSTINDEX;
				else if ("str".equals(cellType))
					nextDataType = xssfDataType.FORMULA;
				else if (cellStyleStr != null) {
					// It's a number, but almost certainly one
					// with a special style or format
					int styleIndex = Integer.parseInt(cellStyleStr);
					XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
					this.formatIndex = style.getDataFormat();
					this.formatString = style.getDataFormatString();
					if (this.formatString == null)
					    this.formatString = BuiltinFormats
						    .getBuiltinFormat(this.formatIndex);
				}
				
			}
			if(name.equals("row")) {
				titles = new HashMap<Integer, Object>();
			}
			// Clear contents cache
			lastContents = "";
		}

		public void endElement(String uri, String localName, String name)
				throws SAXException {
				// Process the last contents as required.
			// Do now, as characters() may be called more than once
				if(nextIsString) {
					int idx = Integer.parseInt(lastContents);
					lastContents = sst.getItemAt(idx).getString();
					nextIsString = false;
				}
				// v => contents of a cell
				// Output after we've seen the string contents
				if(name.equals("v")) {
					String sep = "(";
					String[] cutresult = lastContents.split(Pattern.quote(sep)); 
					String curname = cutresult[0].trim().toLowerCase().replaceAll(" ","_").replaceAll("[^a-zA-Z0-9_]","").replaceAll("__","_");
					String curtext = cutresult[0].trim();
					field = new HashMap<String, String>();
					field.put("name",curname);
					field.put("text",curtext);
					field.put("row",String.valueOf(cellAddress.getRow()));
					field.put("col",String.valueOf(cellAddress.getColumn()));
					titles.put(cellAddress.getColumn(),field);
				}
				if(name.equals("row")) {
					rowcount += 1;
					int foundheader = 0;
					for(Object field:statementfields) {
						HashMap<String,Object> cfield = (HashMap<String,Object>) field;
						for(Map.Entry<Integer,Object> title:titles.entrySet()){
							HashMap<String, String> excelfield = (HashMap<String, String>)title.getValue();
							if(excelfield.get("name").equals(cfield.get("name"))){
								foundheader += 1;
							}
						}
					}

					if(foundheader > 2){
						throw new SAXException("Done reading header and sample");
					}
				}
		}
		public void characters(char[] ch, int start, int length) {
			lastContents += new String(ch, start, length);
		}
	} */
}
