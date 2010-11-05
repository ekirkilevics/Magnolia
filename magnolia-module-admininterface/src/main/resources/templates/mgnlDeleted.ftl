[#assign lastVersion = model.lastVersion!]
[#assign hasLastVersion = lastVersion?has_content]

<html>
  <head>
    <title>${i18n["templates.mgnlDeleted.pageTitle"]}</title>
  </head>
  <body>
<p>This page have been deleted by ${model.deletionAuthor} on ${model.deletionDate}</p>

    [#if hasLastVersion]
      <a href="${ctx.contextPath}/${page.@name}.html?mgnlVersion=${lastVersion}">${i18n["templates.mgnlDeleted.showLastVersion"]}</a></br>
      <a href="${ctx.contextPath}/.magnolia/pages/websiteVersionsList.html?command=restore&repository=website&path=${page.@handle}&versionLabel=${lastVersion}&jsExecutedAfterSaving=
opener.document.location.href=opener.document.location.href;window.close();" target="_blank">${i18n["templates.mgnlDeleted.restoreLastVersion"]}</a></br>
      [#if model.hasChildren()]
        <a href="${ctx.contextPath}/.magnolia/pages/websiteVersionsList.html?command=restoreRecursive&repository=website&path=${page.@handle}&versionLabel=${lastVersion}&jsExecutedAfterSaving=
opener.document.location.href=opener.document.location.href;window.close();" target="_blank">${i18n["templates.mgnlDeleted.restoreLastVersionRecursively"]}</a></br>
      [/#if]
    [#else]
      ${i18n["templates.mgnlDeleted.versioningNotSupported"]}
    [/#if]
  </body>
</html>
