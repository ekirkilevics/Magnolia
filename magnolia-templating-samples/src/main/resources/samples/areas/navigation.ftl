

[#include "/samples/macros/macro.navigation.ftl" ]

[#assign maxDepth = def.parameters.navigationMaxDepth!2]
[#assign rootLevel = def.parameters.navigationRootLevel!0]

[#if rootLevel == 0]
    [#assign startPage = cmsfn.root(content)!]
[#else]
    [#assign startPage = cmsfn.root(content, "mgnl:content")!]
[/#if]


<ul>
    [@renderNavigation startPage maxDepth /]
</ul>


