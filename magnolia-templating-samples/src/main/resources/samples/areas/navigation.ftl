

[#include "/samples/includes/searchForm.ftl" ]


<div id="navigation">

    [#include "/samples/macros/navigation.ftl"]

    [#assign rootPage = cmsfn.root(content, "mgnl:page")!]
    <ul>
        [@renderNavigation rootPage /]
    </ul>

</div><!-- end "navigation" -->








