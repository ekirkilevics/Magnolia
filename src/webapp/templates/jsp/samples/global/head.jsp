<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Magnolia 2.0 Samples | <cms:out nodeDataName="title" /></title>

    <!--  add magnolia css and js links -->
    <cms:links/>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/main.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/richEdit.css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/docroot/samples/js/form.js"></script>

    <c:set var="description"><cms:out nodeDataName="metaDescription" /></c:set>
    <meta name="description" content="${description}" />

    <c:set var="keywords"><cms:out nodeDataName="metaKeywords" /></c:set>
    <meta name="keywords" content="${keywords}" />
</jsp:root>
