Query Node
==========

Query Node is used to specify sql queries to be used.

Example of valid json::
 
  {"q":"ahmad","like":["name","description"]}
 
The above would generate the following where query::

  where name like '%ahmad%' or description like '%ahmad%'
 
To compare equal to field::

  {"q":"ahmad","equal":["name"]}

The above would generate the following::

  where name='ahmad'
 
To compare like for more than 1 field::

  {"like":{"name":"ahmad","description":"balik"}}
 
The above would generate::
 
  where name like '%ahmad%' or description like '%balik%'

Of course to compare directly to value::

  {"equal":{"name":"ahmad","description":"siap"}}
 
The above would generate the following::
 
  where name='ahmad' or description='siap' 

When combining queries, 'or' is the default operator used. To use 'and', place the node in an 'and' node::

  {"and":{"equal":{"name":"ahmad","description":"siap"}}}

That would generate the following query::

  where name='ahmad' and description='siap'
