

<div id="${def.parameters.divId!}">

    [@cms.edit /]

    [#list components as component ]
        [@cms.render content=component /]
    [/#list]

</div><!-- end  ${def.parameters.divId!} -->

