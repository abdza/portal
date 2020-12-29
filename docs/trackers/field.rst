Fields
======

Fields is the column of a database table.

Fields
------

  #) Name : Name of the column in the table
  #) Label : Label to be used in the interface
  #) Type : The data type of the field
  #) Widget : The widget displayed in a form
  #) Option Source : Options for the field

Type
----

The available types are:
  #) String
  #) Text
  #) Integer
  #) Number
  #) Date
  #) DateTime
  #) Checkbox
  #) TreeNode
  #) TrackerType
  #) User

Option Source
-------------

Option Source is a json data structure based on the type of the field.

String
~~~~~~

If the widget is a dropdown, then "Option Source" can be a json array of options for the dropdown.

TrackerType
~~~~~~~~~~~

When field type is TrackerType, then "Option Source" can contain:

  #) module : Module of the tracker the field refers to
  #) slug : Slug of the tracker the field refers to
  #) query : `Query node <query_node.html>`_ to be used to query listing
  #) name_column : The column used to display the value in the drop down of the field
