Getting Started
===============

Download JPF
------------

The easiest way to start using JPF is to just download the JAR file. You can download them from this `page <https://jpf.sanicengine.com>`_. Or you could download the source code from `github <https://github.com/abdza/portal>`_. Make sure to download the JAR file corresponding to the database you want to be using.


Configure JPF
-------------

To start using the JPF. you would first need to configure it. The page above also has ready made template for each type of database for you to kick start your development. Save the template file as application.properties in the same directory as your jar file.

Run JPF
-------

To run it using java, issue the following command in the directory of the jar file.

    java -jar portal-h2.jar

That should start the Java Portal Framework and you can browse it at `http://localhost:6060/ <http://localhost:6000>`_

Setup JPF
---------

To create default admin user and other default settings, go to the following url `http://localhost:6060/setup <http://localhost:6060/setup>`_ and press on the "Setup" button.

You should now be able to login with the following credentials:

  - Username: admin
  - Password: admin123
