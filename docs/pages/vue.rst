Using Vue in Pages
==================

The vue javascript framework can be added to the JPF pages to make them more dynamic.


Including vue into your page
----------------------------

To include vue into your page consists of 3 steps. First you need to define which DOM node vue will be watching. For example::

  <div id="app">
  </div>

Then you need to include the vue.min.js file in the page::

  <th:block layout:fragment="javascript">
    <script type="text/javascript" th:src="@{/libs/vue/vue.min.js}"></script>
  </th:block>

After that you need to declare your script tag with thymeleaf javascript tags enabled::

  <script th:inline="javascript">
    /*<![CDATA[*/
      var app = new Vue({
         el: '#app',
      });
    /*]]>*/
  </script>
 
Example above declares the vue app DOM on the #app div.


Using thymeleaf variables in a vue variable
-------------------------------------------

To use thymeleaf variables and methods, you need to put it in a special tag. For example::

  var turl = /*[[@{/json/company/query}]]*/;

The example above uses the thymeleaf function @ to create a link to the given path. Note the opening and closing tags using slash star and 2 square brackets.
