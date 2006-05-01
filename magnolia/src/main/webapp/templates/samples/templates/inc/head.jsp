<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />

    <!-- exposes the current node for use with jstl -->
    <cms:setNode var="pageProperties" />

    <title>Magnolia 2.1 Samples | ${pageProperties.title}</title>

    <!--  add magnolia css and js links -->
    <cms:links />

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/main.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/richEdit.css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/docroot/samples/js/form.js"><jsp:text></jsp:text></script>

    <meta name="description" content="${pageProperties.metaDescription}" />
    <meta name="keywords" content="${pageProperties.metaKeywords}" />
</jsp:root>
