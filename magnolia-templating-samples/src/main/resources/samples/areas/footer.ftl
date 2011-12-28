

<div id="footer">

    [@cms.edit/]

    [#list components as component ]
        <div id="footer-element">
            [@cms.render content=component /]
        </div><!-- end ${def.parameters.divID!} -->
    [/#list]

</div><!-- end footer -->