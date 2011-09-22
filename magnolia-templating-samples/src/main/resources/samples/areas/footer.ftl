<div id="${def.name!}">
[@cms.edit/]
[#list components as component ]

  <div id="${def.parameters.divID!}">
    [@cms.render content=component /]
  </div><!-- end ${def.parameters.divID!} -->

[/#list]
</div><!-- end  ${def.name!} -->