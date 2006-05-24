<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
          xmlns:cms="urn:jsptld:cms-taglib" xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">

<jsp:directive.page contentType="text/html; charset=UTF-8"/>

<jsp:directive.page import="info.magnolia.cms.beans.config.Server"/>
<jsp:directive.page import="info.magnolia.context.MgnlContext"/>
<jsp:directive.page import="info.magnolia.cms.security.Authenticator"/>
<jsp:directive.page import="info.magnolia.cms.security.User"/>
<jsp:directive.page import="info.magnolia.module.admininterface.Navigation"/>
<jsp:directive.page import="info.magnolia.cms.beans.config.ModuleRegistration"/>
<jsp:directive.page import="java.util.List"/>
<jsp:directive.page import="java.util.ArrayList"/>
<jsp:directive.page import="java.util.Iterator"/>
<jsp:directive.page import="info.magnolia.cms.beans.config.ModuleLoader"/>
<jsp:directive.page import="info.magnolia.cms.module.Module"/>

<jsp:scriptlet>
    // is a system restart needed
    List restartNeedingModules = new ArrayList();

    // collect the modules needing a restart
    for (Iterator iter = ModuleLoader.getInstance().getModuleInstances().keySet().iterator(); iter.hasNext();) {
        String moduleName = (String) iter.next();
        Module module = ModuleLoader.getInstance().getModuleInstance(moduleName);
        if(module.isRestartNeeded()){
            restartNeedingModules.add(module);
        }
    }

    pageContext.setAttribute("restartNeedingModules", restartNeedingModules);
</jsp:scriptlet>
<!--
<jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
</jsp:text>
-->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><fmt:message key="central.title"/></title>

    <link rel="shortcut icon" href="${pageContext.request.contextPath}/.resources/admin-images/favicon.ico"
          type="image/x-icon"/>
    <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/.resources/admin-css/admin-all.css"/>

</head>

<body class="mgnlBgDark mgnlAdminMain">
    <div class="mgnlAdminCentralMessagesDiv mgnlText" >
        <fmt:message>system.restart</fmt:message>:
        <c:forEach items="${restartNeedingModules}" var="module">
            <div style="padding-left: 15px; padding-top: 5px;">- ${module.name} (${module.moduleDefinition.version})</div>
        </c:forEach>
    </div>
</body>

</html>
</jsp:root>
