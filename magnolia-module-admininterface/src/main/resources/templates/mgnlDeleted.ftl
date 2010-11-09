[#assign lastVersion = model.lastVersion!]
[#assign hasLastVersion = lastVersion?has_content]

<html>
  <head>
    <title>${i18n["templates.mgnlDeleted.pageTitle"]}</title>
    <script src="${ctx.contextPath}/.magnolia/pages/javascript.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="${ctx.contextPath}/.resources/admin-css/deletedPage.css">
  </head>
  <body>
    <div class="mgnlMessageBox">
    <h1>${i18n["templates.mgnlDeleted.pageDeleted"]}</h1>
    <p>${i18n["templates.mgnlDeleted.pageDeleted.message"]}</p>
    <p id="mgnlPageDeletedOptions">${i18n["templates.mgnlDeleted.options"]}</p>
    [#if hasLastVersion]
    <div id="mgnlPageDeletedOptions" class="mgnlTreeMenuEmbedded">
      <a id="mgnlPageDeletedOptions_showPrevious" class="mgnlTreeMenuItemEmbedded" href="${ctx.contextPath}/${page.@name}.html?mgnlVersion=${lastVersion}" target="_blank">
        <img class="mgnlTreeMenuItemIcon" src="${ctx.contextPath}/.resources/admin-images/showPreviousVersion.gif"/>
        <div class="mgnlTreeMenuItemCommand">${i18n["templates.mgnlDeleted.showLastVersion"]}</div>
      </a>
      <a id="mgnlPageDeletedOptions_undelete" class="mgnlTreeMenuItemEmbedded"
           href="${ctx.contextPath}/.magnolia/pages/websiteVersionsList.html?command=restore&repository=website&path=${page.@handle}&versionLabel=${lastVersion}&jsExecutedAfterSaving=opener.document.location.href=opener.document.location.href;MgnlAdminCentral.showTree('website','${page.@handle}');window.close();" target="_blank">
        <img class="mgnlTreeMenuItemIcon" src="${ctx.contextPath}/.resources/admin-images/restorePreviousVersion.gif"/>
        <div class="mgnlTreeMenuItemCommand">${i18n["templates.mgnlDeleted.restoreLastVersion"]}</div>
      </a>
      [#if model.hasChildren()]
        <a id="mgnlPageDeletedOptions_undelete" class="mgnlTreeMenuItemEmbedded"
           href="${ctx.contextPath}/.magnolia/pages/websiteVersionsList.html?command=restoreRecursive&repository=website&path=${page.@handle}&versionLabel=${lastVersion}&jsExecutedAfterSaving=opener.document.location.href=opener.document.location.href;MgnlAdminCentral.showTree('website','${page.@handle}');window.close();" target="_blank">
          <img class="mgnlTreeMenuItemIcon" src="${ctx.contextPath}/.resources/admin-images/restorePreviousVersion.gif"/>
          <div class="mgnlTreeMenuItemCommand">${i18n["templates.mgnlDeleted.restoreLastVersionRecursively"]}</div>
        </a>
      [/#if]
      [#else]
          <div class="mgnlTreeMenuItemCommand">${i18n["templates.mgnlDeleted.versioningNotSupported"]}</div>
      [/#if]
      <a id="mgnlPageDeletedOptions_gotoCentral" class="mgnlTreeMenuItemEmbedded" href="javascript:MgnlAdminCentral.showTree('website','${page.@handle}'); window.close();">
        <div id="mgnlPageDeletedOptions_gotoCentralCommand" class="mgnlTreeMenuItemCommand">${i18n["templates.mgnlDeleted.toAdminCentral"]}</div>
      </a>
    </div>
  </div>
  </body>
</html>
