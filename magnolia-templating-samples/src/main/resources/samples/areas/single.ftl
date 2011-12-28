

<div id="${def.parameters.divId!}">

    [@cms.edit/]

    [#if component?exists]
        [@cms.render content=component /]
    [/#if]

</div><!-- end  ${def.parameters.divId!} -->
