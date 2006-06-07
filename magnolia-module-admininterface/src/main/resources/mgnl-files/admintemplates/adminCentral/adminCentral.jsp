<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
  xmlns:cms="urn:jsptld:cms-taglib" xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
  <jsp:directive.page contentType="text/html; charset=UTF-8" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.Server" />
  <jsp:directive.page import="info.magnolia.context.MgnlContext" />
  <jsp:directive.page import="info.magnolia.module.admininterface.Navigation" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.ModuleRegistration" />
  <jsp:directive.page import="java.util.List" />
  <jsp:directive.page import="java.util.ArrayList" />
  <jsp:directive.page import="java.util.Iterator" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.ModuleLoader" />
  <jsp:directive.page import="info.magnolia.cms.module.Module" />
  <jsp:directive.page import="info.magnolia.cms.license.LicenseFileExtractor"/>
  <jsp:scriptlet>
    <![CDATA[
    // create the menu
    Navigation navigation = new Navigation("/modules/adminInterface/config/menu", "mgnlNavigation");
    pageContext.setAttribute("navigation", navigation);

        ]]>
  </jsp:scriptlet>
  <!--
    <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>
  -->
  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <title>
        <fmt:message key="central.title" />
      </title>
      <link rel="shortcut icon" href="${pageContext.request.contextPath}/.resources/admin-images/favicon.ico"
        type="image/x-icon" />
      <link rel="stylesheet" type="text/css"
        href="${pageContext.request.contextPath}/.resources/admin-css/admin-all.css" />
      <!--  keep the old javascripts -->
      <script type="text/javascript" src="${pageContext.request.contextPath}/admintemplates/js/admincentral.jsp"><!-- --></script>
      <script type="text/javascript">
        <![CDATA[
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
            if (opener &&  opener.MgnlAdminCentral && opener.MgnlAdminCentral.onOpenedInNewWindow) {
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
        ]]>
      </script>
    </head>
    <body class="mgnlBgDark mgnlAdminMain">
      <!-- logo -->
      <a href="http://www.magnolia.info" target="_blank">
        <img style="position:absolute; top:17px; left:20px" border="0" src="${pageContext.request.contextPath}/.resources/admin-images/magnoliaLogo.gif"/>
      </a>
      
      <!--  user info -->
      <cms:user var="mgnluser" anonymous="true" />
      <c:if test="${!empty(mgnluser.name)}">
        <div style="position:absolute;top:44px;right:20px;text-align:right;" class="mgnlText">
          <fmt:message key="central.user"/>: ${mgnluser.name}
          |
          <a href="${pageContext.request.contextPath}/.magnolia/pages/logout.html" style="color: black;">
            <fmt:message key="central.logout" />
          </a>
        </div>
      </c:if>
      
      <!-- Menu -->
      <div id="mgnlAdminCentralMenuDiv" class="mgnlAdminCentralMenuDiv">
        <div class="mgnlAdminCentralMenu">
          <!-- do not delete me -->
        </div>
      </div>
      
      <!-- Not scrolled content like the website tree -->
      <div id="mgnlAdminCentralContentDiv" class="mgnlAdminCentralContentDiv">
        <iframe id="mgnlAdminCentralContentIFrame" src="" scrolling="no" style="border: none; width:100%; height:100%"
          frameborder="0">
          <![CDATA[<!-- a comment here is needed for the correct rendering of the iframe tag -->]]>
        </iframe>
      </div>
      <!-- Scrolled content like the about or other included pages -->
      <div id="mgnlAdminCentralScrolledContentDiv" class="mgnlAdminCentralContentDiv">
        <iframe id="mgnlAdminCentralScrolledContentIFrame" src="" style="border: none; width:100%; height:100%"
          frameborder="0">
          <![CDATA[ <!-- a comment here is needed for the correct rendering of the iframe tag -->]]>
        </iframe>
      </div>
      
      <!-- About -->
      <div id="mgnlAdminCentralFooterDiv" style="position:absolute;text-align:center;" class="mgnlText">
           <a href="http://www.magnolia.info" target="_blank">Magnolia</a>™ 
           Simple Enterprise Content Management. 
           (<jsp:expression>
                LicenseFileExtractor.getInstance().get(LicenseFileExtractor.EDITION)
           </jsp:expression>,
            <jsp:expression>
                LicenseFileExtractor.getInstance().get(LicenseFileExtractor.VERSION_NUMBER)
            </jsp:expression>) -
            <a href="http://jira.magnolia.info" target="_blank">Bug/feature request</a>
        </div>
    </body>
  </html>
</jsp:root>
