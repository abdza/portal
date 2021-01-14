JPF Page Template Mode
======================

A JPF Page in template mode will render html output from a thymeleaf template. There are a few layouts from which we can choose to expand to make our job easier. The most basic is the base.html. On top of that template is built the main and special template. The main template is used when you want to include all the interface features. The special template is used when you do not need the other features like side menu and others. The formbase template includes jsp and css specifically used for input forms. And treebase includes jsp and css which is used specifically to display the tree structure.

==================   ===========  ===========  ===========  ===========  ==========
Fragment              Base         Main         Special      Formbase     Treebase
==================   ===========  ===========  ===========  ===========  ==========
head                  O             O             O            O            O
head-meta             X             X             X            O            X
head-title            O             O             O            O            O
head-end              X             X             X            O            O
body-container        O             O             O            O            O
navbar                              O                          O            O
sidebar                             O                          O            O
breadcrumb                          O
content               X             X             X            X            X
footer                              O                          O            O
base-javascript       O             O             O            O            O
layout-javascript     X             X             X            O            O
page-javascript       X             X             X            X            X
javascript            X             X             X            X            X
==================   ===========  ===========  ===========  ===========  ==========

The above table show a list of fragments which the page template can overwrite. X marks the fragment as available and not used yet, it is there just to be overwritten by other templates. O marks the fragment as used, either in the layout template itself or from the template it inherits. So to overwrite those fragments, it would be wise to check on the template source code to be more acquainted with what would be overwritten and whether you'd need to replace it or not.
