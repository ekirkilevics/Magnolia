    MgnlDHTMLUtil.addOnLoad(${functionBar.javascriptName}Init);
    
    var ${functionBar.javascriptName};
    
    function ${functionBar.javascriptName}Init(){
        ${functionBar.javascriptName} = new MgnlFunctionBar ('${functionBar.javascriptName}');

        <#if functionBar.hasMenuItems()>
            <#list functionBar.menuItems as item>
                <#if item?exists>
                    ${functionBar.javascriptName}.addNode ("${item.name}", ${item.active?string}, "${item.icon?default('')}", "${item.inactiveIcon?default('')}", "${item.onclick}");
                    // conditions to acitvate this item
                    <#list item.javascriptConditions as cond>
                        ${functionBar.javascriptName}.getNode ("${item.name}").addCondition(${cond});
                    </#list>
                </#if>
            </#list>
        </#if>
        ${functionBar.javascriptName}.refresh ();
    }

