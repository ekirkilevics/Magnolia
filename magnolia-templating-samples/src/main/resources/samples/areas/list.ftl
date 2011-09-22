<div id="${def.name!}">

[@cms.edit /]

[#list components as component ]
    [@cms.render content=component /]
[/#list]

</div><!-- end  ${def.name!} -->

