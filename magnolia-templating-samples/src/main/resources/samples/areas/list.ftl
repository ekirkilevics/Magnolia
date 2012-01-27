

<div id="${def.parameters.divId!}">


    [#list components as component ]
        [@cms.component content=component /]
    [/#list]

</div><!-- end  ${def.parameters.divId!} -->

