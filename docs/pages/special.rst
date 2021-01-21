Java Portal Framework Special Pages
===================================

There are several slugs reserved with special functions in the JPF. They are listed here.


Sidebar Menu
------------

A page with the module 'portal' and slug 'sidebar_menu' would be used to display the additional menu in the sidebar. Usually it would just consist of a series of ul links. For example::

  <li class="nav-item">
    <a class="nav-link" th:href="@{/survey/survey/list}">Survey</a>
  </li>

If the link should only be seen by a system admin for example, we can set the criteria::

  <li sec:authorize="hasRole('ROLE_SYSTEM_ADMIN')" class="nav-item">
    <a class="nav-link" th:href="@{/admin/users}">Users</a>
  </li>


Navbar Menu
-----------

Just like the sidebar, the navbar can also be customised with a custom page. Just create a page with the module 'portal' and slug 'navbar_menu'.


Home Page
---------

A page with the module 'portal' and slug 'home' would be used as the default home page when user access the root of the site '/'.
