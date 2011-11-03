<div id="${def.name!}">

[#include "/samples/macros/navigation.ftl"]

[#assign maxDepth = def.parameters.navigationMaxDepth!2]
[#assign rootLevel = def.parameters.navigationRootLevel!0]

[#if content?has_content]
  [#if rootLevel == 0]
      [#assign startPage = cmsfn.root(content)!]
  [#else]
      [#assign startPage = cmsfn.root(content, "mgnl:page")!]
  [/#if]
  <ul>
   [@renderNavigation startPage maxDepth /]
  </ul>
[#else]
    <p style="background-color: yellow; color: red; font-weight: bold; padding: 3px">Missing navigation (content is null)</p>
[/#if]

</div><!-- end  ${def.name!} -->



