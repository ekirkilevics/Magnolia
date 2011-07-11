

[#include "/samples/macros/macro.navigation.ftl" ]

[#assign maxDepth = def.maxNavigationDepth!2]

<ul>
    [@renderNavigation cmsfn.rootPage(content) maxDepth /]
</ul>


