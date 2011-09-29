
<div id="${def.name!}">

[@cms.edit/]
[#if component??]
    [@cms.render content=component /]
[/#if]
</div><!-- end  ${def.name!} -->
