<!--
 -  Copyright 2005 obinary ag.  All rights reserved.
 -  See license distributed with this file and available
 -  online at http://www.magnolia.info/dms-license.html
 -->

<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <jsp:text> <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]> </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
        <head>
            <c:import url="/templates/jsp/samples/global/head.jsp" />
            <script type="text/javascript">
            		var contextPath = '${pageContext.request.contextPath}';
            	</script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/.resources/js/dms/MgnlDMS.js"><jsp:text></jsp:text></script>
        </head>
        <body>
            <c:import url="/templates/jsp/samples/global/mainBar.jsp" />
            <div id="contentDivMainColumn">
                <c:import url="/templates/jsp/samples/global/columnMain.jsp" />
                <!-- use demo paragraphs -->
                <c:import url="/templates/jsp/dms/demotemplate/columnMainNewBar.jsp" />
                <c:import url="/templates/jsp/samples/global/footer.jsp" />
            </div>
            <div id="contentDivRightColumn">
                <c:import url="/templates/jsp/samples/global/columnRight.jsp" />
            </div>
            <c:import url="/templates/jsp/samples/global/headerImage.jsp" />
            <cmsu:simpleNavigation />
        </body>
    </html>
</jsp:root>