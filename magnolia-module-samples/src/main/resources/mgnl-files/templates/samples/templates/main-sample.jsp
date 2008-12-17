<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${content.title}</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/samples.css" />
    <cms:links/>
</head>
<body>
<cms:mainBar dialog="sampleProperties"/>

<div id="header">
    <h1>Magnolia Samples</h1>
</div>

<div id="navigation"><cmsu:simpleNavigation/></div>

<div id="main">
    <h2>${content.title}</h2>
    <p>${content.text!""}</p>

    <cms:contentNodeIterator contentNodeCollectionName="main">
        <cms:includeTemplate/>
    </cms:contentNodeIterator>

    <cms:newBar contentNodeCollectionName="main" paragraph="sampleJSP, sampleJSPSearch, sampleJSPControlsShowRoom"/>

</div>

<div id="footer">
    <p>This page was last edited by <span class="author"><cms:out nodeDataName="mgnl:authorid" contentNodeName="MetaData"/></span>
        on <span class="modificationdate"><cms:out nodeDataName="mgnl:lastmodified" contentNodeName="MetaData"/></span></p>
</div>

</body>
</html>
