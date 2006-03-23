<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
          xmlns:cms="urn:jsptld:cms-taglib" xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">

<jsp:directive.page contentType="text/html; charset=UTF-8"/>

<jsp:directive.page import="info.magnolia.cms.beans.config.Server"/>
<jsp:directive.page import="info.magnolia.cms.beans.runtime.MgnlContext"/>
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
    // create the menu
    Navigation navigation = new Navigation("/modules/adminInterface/config/menu", "mgnlNavigation");
    
    // get the current username
    User user = MgnlContext.getUser();
    String userName = "";
    if (user == null || (userName = user.getName()).equals("")) userName = Authenticator.getUserId(request);

    pageContext.setAttribute("navigation", navigation);
    pageContext.setAttribute("username", userName);

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

    <!--  keep the old javascripts -->
    <script type="text/javascript"
            src="${pageContext.request.contextPath}/admintemplates/js/admincentral.jsp"><!-- --></script>

    <script type="text/javascript">
        importClass("mgnl.admininterface.AdminCentral");
        importClass("mgnl.admininterface.Navigation");

        var mgnlNavigation;
        var mgnlAdminCentral;

        function configureNavigation() {
            ${navigation.javascript}
        }

        // init the system
        onload = function() {
            // start the admin central
            mgnlAdminCentral = new MgnlAdminCentral();

            // create and configure the navigation
            mgnlNavigation = new MgnlNavigation();
            configureNavigation();

            // init navigation
            mgnlNavigation.create('mgnlAdminCentralMenuDiv');

            // call the onload method
            if (opener <![CDATA[ && ]]> opener.MgnlAdminCentral <![CDATA[ && ]]> opener.MgnlAdminCentral.onOpenedInNewWindow) {
                opener.MgnlAdminCentral.onOpenedInNewWindow(mgnlAdminCentral);
            }
            else {
                // call the frist menupoint with a link
                mgnlNavigation.activate('${navigation.firstId}', true);
            }

            // resize
            mgnlAdminCentral.resize();

            // on resize
            onresize = function() {
                mgnlAdminCentral.resize()
            };
        }
    </script>
</head>

<body class="mgnlBgDark mgnlAdminMain">

    <!-- Menu -->
    <div id="mgnlAdminCentralMenuDiv" class="mgnlAdminCentralMenuDiv">
        <div class="mgnlAdminCentralMenu">
            <!-- do not delete me -->
        </div>
    </div>
    
    <!-- Not scrolled content like the website tree -->
    <div id="mgnlAdminCentralContentDiv" class="mgnlAdminCentralContentDiv">
        <iframe
                id="mgnlAdminCentralContentIFrame" src="" scrolling="no"
                style="border: none; width:100%; height:100%" frameborder="0"><![CDATA[
            <!-- a comment here is needed for the correct rendering of the iframe tag -->]]></iframe>
    </div>
    
    <!-- Scrolled content like the about or other included pages -->
    
    <div id="mgnlAdminCentralScrolledContentDiv" class="mgnlAdminCentralContentDiv">
        <iframe
                id="mgnlAdminCentralScrolledContentIFrame" src="" style="border: none; width:100%; height:100%"
                frameborder="0"><![CDATA[ <!-- a comment here is needed for the correct rendering of the iframe tag -->]]>
        </iframe>
    </div>

</body>
</html>
</jsp:root>
