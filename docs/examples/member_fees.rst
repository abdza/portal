JPF Example - Member Fees
=========================

This is an example of a simple project developed using the Java Portal Framework. The premise of the project is that you are the treasurer of a club you belong to and you need to keep track of the fees paid by your club members. This would be an offline system that you manage yourself, thus there will be no user management or anything like that. The system would be able to do the following things:

  #. Register/edit member information
  #. Manage fees type
  #. Record fees payment
  #. Print payment receipt

To achieve this, we would do the following steps:

  #. Create a tracker to manage member information
  #. Create a tracker to manage the type of fees
  #. Create a tracker to manage the payment
  #. Create a page to print receipt

To start, follow the steps in the `Getting Started <../started/intro.html>`_ page to setup your base system.

Create the member tracker
-------------------------

  #. First step to create the member tracker is to login as Admin into your system.
  #. Click on the "Trackers" menu in the Sidebar menu.
  #. Click on the "Add" button under the "Tracker Listing".
  #. In the form, fill in the following values:
       #. Module: club
       #. Slug: member
       #. Name: Club Members
       #. Type: Statement
  #. Click on the "Save" button.
  #. Once we've created the tracker, we need to add the fields for it. Click on the "Fields" tab at the "Tracker Info" page.
  #. Click on the "Add" button to add a new field.
  #. In the form, fill in the following values:
       #. Name: name
       #. Label: Name
       #. Type: String
       #. Widget: Default
  #. Click on the "Save" button to save the field.
  #. Repeat step 7-9 for the following fields
      +-----------------+---------------+---------------+---------------+
      |Name             |Label          |Type           |Widget         |
      +=================+===============+===============+===============+
      |phone_number     |Phone Number   |String         |Default        |
      +-----------------+---------------+---------------+---------------+
      |address          |Address        |Text           |Default        |
      +-----------------+---------------+---------------+---------------+
      |e_mail           |E-Mail         |String         |Default        |
      +-----------------+---------------+---------------+---------------+
      |member_since     |Member Since   |Date           |Default        |
      +-----------------+---------------+---------------+---------------+
  #. Once we have created all the fields, we need to update the database table for the tracker. Click on the "Details" tab, then on the "Update DB" button to update the database table for the tracker.
  #. Then we need to add the fields to view in the items listing. Click on the "Edit" button. Then add the fields we wish to display in the "List Fields". So add the following values in the "List Fields" field, "name, phone_number, address, e_mail, member_since". Then click on the "Save" button to update the tracker.
  #. Now we can start using the tracker. Click on the "View" button to view the "Club Members" tracker data.
  #. You can test the tracker by adding a new record. Press on the "Add" button. Fill in some test data in the form. Then click on the "Save" button to insert the information into the database.
  #. You should see it added to the data listing. You can view the record details by clicking on the item number on the very left column. In the details page, you can click on the "Edit" button to update the information for that record. Click on the "List" button to go back to the record listing.
  #. You can also try to delete information by clicking on the "Delete" button, then confirm the delete by clicking on the "OK" button or "Cancel" to cancel the process.
