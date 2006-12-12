<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
    xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />

    <!-- exposes the current node for use with jstl -->
    <cms:setNode var="pageProperties" />

    <title>Magnolia 3.0 Samples | ${pageProperties.title}</title>

    <!--  add magnolia css and js links -->
    <cms:links />

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/main.css" />

    <meta name="description" content="${pageProperties.metaDescription}" />
    <meta name="keywords" content="${pageProperties.metaKeywords}" />
</jsp:root>
