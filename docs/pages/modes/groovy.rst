Using Groovy in a JPF Page
==========================

A JPF Page that can be run, will be run using groovy. Since groovy is a superset of java, you can write your code just like how you would write your java program, or you could just take advantage of the extra syntax sugar groovy affords you.

The variables made available for the developer are:

  #. postdata :- the POST value submitted by the user
  #. arg1,arg2,arg3,arg4,arg5 :- Additional variables the developer can use optionally from the url
  #. request :- the request object of this current request
  #. env :- the enviroment object of this current request. Can be used to access variables set in the runtime environment or even application.properties settings
  #. namedjdbctemplate :- the namedjdbctemplate object which the developer can use to run custom queries to the database
  #. javaMailSender :- the javamailsender object to use to send emails
  #. passwordEncoder :- the password encoder in case the page processing requires to hash user password
  #. trackerService :- the service to manage and manipulate trackers and their data
  #. userService :- the service to access information about users including the currently login user
  #. settingService :- the service to manage and use settings
  #. fileService :- the service to manage and use files uploaded as FileLink
  #. treeService :- the service used to manage and manipulate tree structures in the portal

Manipulating Excel Output
-------------------------

If the page is accessed through url http://<server>/excel/<module>/<slug> would allow for an excel file to be customised. The template would already have variable wb and sheet for the workbook and excel sheet::

  import org.apache.poi.ss.usermodel.Row;
  import org.apache.poi.ss.usermodel.Cell;

  Row headerRow = sheet.createRow(0);

  def headers = ['Name','Group','Test'];
  for(int i=0;i<headers.size();i++) {
    Cell cell = headerRow.createCell(i);
    cell.setCellValue(headers[i]);
  }

The above example would create a simple excel file with 3 columns.
