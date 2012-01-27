

<div id="footer">


    [#list components as component ]
        <div id="footer-element">
            [@cms.component content=component /]
        </div><!-- end ${def.parameters.divID!} -->
    [/#list]

</div><!-- end footer -->