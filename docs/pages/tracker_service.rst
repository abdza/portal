Tracker Service
===============

Tracker Service is used to access tracker data.

Example of how to use the methods.

How to get a single row of data with it's id
--------------------------------------------

In a template page, you would use the datarow function. For example::
  
  <section layout:fragment="content" class='page-content' th:with="survey=${@trackerService.datarow('survey','survey',arg1)}">

The code above would assign the variable survey with the data from the tracker with the module "survey" and slug "survey" where the id is equal to arg1.

How to get multiple rows of data from tracker
---------------------------------------------

The easiest way is to use the dataRows function. Note the capital letter R, and S at the end. For example::

  <li th:each="survey : ${@trackerService.dataRows('survey','survey')}">

The code above would loop over every record from the tracker with module "survey" and slug "survey" and assign it to the variable survey.

How to access row properties in the template
--------------------------------------------

For example::

  <a href="#" th:text="${survey.name}" th:href="@{/view/survey/run/{id}(id=${survey.id})}">Survey</a>

The above would display the survey name as a link to "/view/survey/run/<survey id>".

How to filter the query to the tracker
--------------------------------------

You would use a `query json <../trackers/query_node.html>`_ in your dataRows method. For example::

  <div th:each="question : ${@trackerService.dataRows('survey','question',{'equal':{'survey':survey.id},'order':['pos_num asc']})}">

The above code would filter the tracker with module 'survey' and slug 'question', where the variable survey in the tracker(question) is equal to the variable id in the current survey variable. The results would be ordered by the column pos_num in ascending order. It would loop over each row and assigning the results into the variable question.

Save Data In a Runable Page
---------------------------

To save data in a runable page, you can use the saveMap method to do it. For example::

  def ss = trackerService.saveMap('survey','survey_session',['survey':postdata['survey_id'][0]]);

The above code would save into the tracker with module 'survey' and slug 'survey_session'. The field 'survey' would be set to what was submitted. postdata is automatically provided and consist of an array of each field that was sent. If only 1 value was submitted with that particular name, then just refer to item 0 like above.
