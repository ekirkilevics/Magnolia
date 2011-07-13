

[#list components as component ]

    <div id="${def.parameters.divID!}">
        [@cms.render content=component /]
    </div><!-- end ${def.parameters.divID!} -->

[/#list]

