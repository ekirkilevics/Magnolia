     <!-- line for moving to the bottom -->
    <div id="${lineId}" class="mgnlTreeLineInter mgnlLineEnabled" onmouseover="${tree.javascriptTree}.moveNodeHighlightLine('${lineId}');" onmouseout="${tree.javascriptTree}.moveNodeResetLine('${lineId}');"
        onmousedown="${tree.javascriptTree}.pasteNode('${tree.path}',${PASTETYPE_SUB},${permissionWrite?string},'${lineId}');"></div>
    
    <!-- this is needed for IE else you can't scroll. Do not ask me why! -->
    <div style="position:absolute;" >&nbsp;</div>
    
    <!-- do we have write permission on the root? -->
    <input type="hidden" name="${tree.javascriptTree}_${tree.path}_PermissionWrite" id="${tree.javascriptTree}_${tree.path}_PermissionWrite" value="${permissionWrite?string}" />

<!--  close the div opened in the header -->
</div>

<!-- for moving -->
<div id="${tree.javascriptTree}_MoveShadow" style="position:absolute;top:0px;left:0px;visibility:hidden;background-color:#fff;"></div>

<img src="${contextPath}${DOCROOT}move_denied.gif" id="${tree.javascriptTree}_MoveDenied" style="position:absolute;top:0px;left:0px;visibility:hidden;" />

<!-- menu divs -->
<#if menu?exists && (menu.menuItems?size >0)>
    ${menu.html}
</#if>

<#if tree.browseMode>
    <#include "TreeAddressBar.ftl"/>
</#if>

<#if !tree.browseMode && tree.functionBar?exists>
    ${tree.functionBar.html}
</#if>

<#include "TreeJavascript.ftl"/>

