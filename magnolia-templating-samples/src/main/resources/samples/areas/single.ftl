

<div id="${def.parameters.divId!}">


    [#if component?exists]
        [@cms.component content=component /]
    [/#if]

</div><!-- end  ${def.parameters.divId!} -->
