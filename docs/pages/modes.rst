JPF Page Modes
==============

A JPF page is a basic components for any JPF module. A page can be in either of 3 modes. The modes are:

  #. Template : A page in template mode will output html using thymeleaf as the templating language.
  #. Redirect : A page in redirect mode can consist of source code in groovy and redirect to another page after the page has been executed.
  #. JSON : A page in json mode consist of source code in groovy and will return the return value in json format.
  #. API : A page in api mode consist of source code in groovy and will return the return value in json format. Main difference with JSON mode is that it will not validate form submission with csrf as it will use the basic auth header to validate user for the use in an api.

.. toctree::
   :maxdepth: 1
   :caption: Contents:

   modes/template
   modes/redirect
   modes/json
   modes/api
