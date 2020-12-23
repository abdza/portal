Getting Started
===============

Download JPF
------------

The first step in using the JPF is to download it from github.com. You can find the link
here `portal.jar <https://github.com/abdza/portal/raw/origin/release/0.0.1/portal.jar>`_. 


Configure JPF
-------------

To start using the JPF. you would first need to configure it. `Here <https://github.com/abdza/portal/raw/origin/release/0.0.1/application.properties.sample>`_ is a ready made template 
for you to kick start your development. Save the template file as application.properties in 
the same directory as your jar file.

Run JPF
-------

To run it using java, issue the following command in the directory of the jar file.

    java -jar portal.jar

That should start the Java Portal Framework and you can browse it at `http://localhost:6060/ <http://localhost:6000>`_

Setup JPF
---------

To create default admin user and other default settings, go to the following url `http://localhost:6060/setup <http://localhost:6060/setup>`_ and press on the "Setup" button.

You should now be able to login with the following credentials:

  - Username: admin
  - Password: admin123
