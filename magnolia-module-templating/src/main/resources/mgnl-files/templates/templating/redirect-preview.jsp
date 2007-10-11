<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
          xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt">
<jsp:directive.page contentType="text/html; charset=UTF-8" session="false"/>
<jsp:text>
<![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
</jsp:text>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<!-- exposes the current node for use with jstl -->
    <cms:setNode var="pageProperties"/>
    <title>Magnolia - Redirect ${pageProperties.title}</title>
    <!--  add magnolia css and js links -->
    <cms:links/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/main.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/docroot/samples/js/form.js">
        <jsp:text><!--  --></jsp:text>
    </script>
    <meta name="description" content="${pageProperties.metaDescription}"/>
    <meta name="keywords" content="${pageProperties.metaKeywords}"/>
</head>
<body>
<cms:mainBar paragraph="redirect-page-properties"/>
<div>
    <p>Should redirect to <cms:out nodeDataName="location"/></p>
</div>
</body>
</html>
</jsp:root>
