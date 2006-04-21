<style>
    div.mgnlFunctionBar{
        background-color: #EBEEDE;
        padding:2px;
        font-family: verdana;
        font-size: 10px;
        border: thin solid;
        border-left-color: #999;
        border-top-color:#ccc;
        border-bottom-color: #ccc;
        border-right-color: #ccc;
        height: 42px;
    }
    input.queryField {
		border-top: 1px #999999 solid;
		border-bottom: 1px #CCCCCC solid;
		border-left: none;
		border-right: none;
		height: 20px;
		font:11px verdana, arial, helvetica, sans-serif;
		z-index:1;
	}
	div.mgnlFunctionBar td.separator {
		border-left: 1px solid #B2B2B2 !important;
		width:10px;
	}
	td.mgnlFunctionBarButton {
		padding: 0 15px 0 5px;
	}
	td.mgnlFunctionBarButton a {
		padding: 2px;
		text-align: center;
		font-size: 10px;
		cursor: pointer;
		display: table-cell;
		text-decoration: none;
		color: black;
	}
	td.mgnlFunctionBarButton a:hover {
		text-align: center;
		font-size: 10px;
		cursor: pointer;
		background-color: #FBFEEE;
		display: table-cell;
		text-decoration: none;
		color: black;
	}
	/* inactive stuff */
	td.mgnlFunctionBarButtonInactive {
		padding: 0 15px 0 5px;
	}
	td.mgnlFunctionBarButtonInactive a {
		padding: 2px;
		text-align: center;
		font-size: 10px;
		cursor: default;
		color: gray;
		display: table-cell;
		text-decoration: none;
	}
</style>

<div id="mgnlFunctionBarDiv" class="mgnlFunctionBar">
  <table cellpadding="0" cellspacing="0" border="0" width="100%" border="0">
    <tr>
      <td>
        <table cellpadding="0" cellspacing="0" style="empty-cells: show; height: 42px;" border="0">
          <tr>
<#if functionBar.hasMenuItems()>
  <#list functionBar.menuItems as item>
    <#if item?exists>
            <td class="mgnlFunctionBarButtonInactive" id="${functionBar.javascriptName}_${item.name}">				
              <div><a href="javascript:void(0);" class="mgnlFunctionBarAnchor" onclick="${functionBar.javascriptName}.clicked ('${item.name}')" id="${functionBar.javascriptName}_${item.name}_div">
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
            
          var mgnlSearchTF = new MgnlTextField ('${functionBar.javascriptName}SearchField', '', 200, contextPath + '/.resources/controls/search/lupe.gif', 29, {onKeyPress: "if (mgnlIsKeyEnter(event)) {${functionBar.onSearch}}"});
          document.write (mgnlSearchTF.render ());
        </script>
      </td>
      </#if>
    </tr>
  </table>
</div>
