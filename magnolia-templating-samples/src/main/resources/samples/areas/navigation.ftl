

[#include "/samples/macros/macro.navigation.ftl"]

[#assign maxDepth = def.parameters.navigationMaxDepth!2]
[#assign rootLevel = def.parameters.navigationRootLevel!0]
[#if content??]
  [#if rootLevel == 0]
      [#assign startPage = cmsfn.root(content)!]
  [#else]
      [#assign startPage = cmsfn.root(content, "mgnl:content")!]
      <ul>
       [@renderNavigation startPage maxDepth /]
      </ul>
  [/#if]
[#else]
    <p style="background-color: yellow; color: red; font-weight: bold; padding: 3px">Missing navigation (content is null)</p>
[/#if]

