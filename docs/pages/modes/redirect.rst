JPF Page Redirect Mode
======================

A JPF in redirect mode require to be marked as runable. A page marked as runable and the type is redirect will run the contents of the content field using groovy and use the return value as a string to redirect to. An example use case of this is for example you create a page to process a form submission. So you can write the processing of the page submission (it will be made available in a hashmap variable named postdata) to create whatever object you need or start whatever process you require, then once all is done, redirect to another page which could very well be a template page saying thank you to the user for their submission.

The variables made available for the developer are:

  #. postdata :- the POST value submitted by the user
  #. arg1,arg2,arg3,arg4,arg5 :- Additional variables the developer can use optionally in the url
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

To program using groovy is explained further in the `Using Groovy in a JPF Page <groovy.html>`_ section.
