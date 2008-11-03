<div id="mgnlFunctionBarDiv" class="mgnlFunctionBar">
  <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
      <td>
        <table cellpadding="0" cellspacing="0" style="empty-cells: show; height: 42px;" border="0">
          <tr>
<#if functionBar.hasMenuItems()>
  <#list functionBar.menuItems as item>
    <#if item?exists>
            <td class="mgnlFunctionBarButtonInactive" id="${functionBar.javascriptName}_${item.name}">
              <div><a class="mgnlFunctionBarAnchor" href="javascript:${functionBar.javascriptName}.clicked ('${item.name}')" id="${functionBar.javascriptName}_${item.name}_div">
              <#if item.icon?exists><img src="${item.icon}" id="${functionBar.javascriptName}_${item.name}_img" border="0" alt="" /><br/></#if>
              ${item.label}</a></div>
            </td>
    <#else>
            <td class="separator"><div><!-- --></div></td>
    </#if>
  </#list>
<#else>
            <td class="mgnlFunctionBarButtonInactive"><div><!-- --></div></td>
</#if>
          </tr>
        </table>
      </td>
      <#if functionBar.searchable>
      <td style="width: 2%; padding-right: 10px; vertical-align: middle;">
        <script type="text/javascript">
          var mgnlSearchTF = new MgnlTextField ('${functionBar.javascriptName}SearchField',"${functionBar.searchStr?html}" , 200, contextPath + '/.resources/controls/search/lupe.gif', 29, {onKeyPress: "if (mgnlIsKeyEnter(event)) {(${functionBar.onSearchFunction})($('${functionBar.javascriptName}SearchField').value)}"});
          document.write (mgnlSearchTF.render());
        </script>
      </td>
      </#if>
    </tr>
  </table>
</div>
