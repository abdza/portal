JPF Page Modes
==============

A JPF page is a basic components for any JPF module. A page can be in either of 3 modes. The modes are:

  #. Template : A page in template mode will output html using thymeleaf as the templating language.
  #. Redirect : A page in redirect mode can consist of source code in groovy and redirect to another page after the page has been executed.
  #. JSON : A page in json mode is like a redirect mode page where the page consist of groovy code. But rather than redirect to another page after it has been executed, it will return the return value in json format.

.. toctree::
   :maxdepth: 1
   :caption: Contents:

   modes/template
   modes/redirect
   modes/json
