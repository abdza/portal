JPF Page API Mode
==================

A JPF in api mode require to be marked as runable. A page marked as runable and the type is api will run the contents of the content field using groovy and return the return value as a json object.  An example use case of this is for example you create a page to provide data for an ajax process. Typically a json page would be accessed using the following url:
  http://<server_root>/api/<module>/<slug>/<arg1>/<arg2>/<arg3>/<arg4>/<arg5>

Where the arg1 until arg5 is just optional. 

The main difference between an API page and a JSON page is the way form submission is handled. The JSON page will only accept form data that is protected by csrf. But the API page will ignore csrf requirement for forms. But an API page will use the basic auth header to verify the user identity. This allows for POST submission to be sent to the endpoint even from a mobile app or other sources.

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
