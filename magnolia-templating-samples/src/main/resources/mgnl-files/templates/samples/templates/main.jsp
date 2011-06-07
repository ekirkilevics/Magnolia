<!-- -------------- Sample JSP Template -------------- -->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<html>
  <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
      <title>
        <cms:ifNotEmpty nodeDataName="title">${content.title}</cms:ifNotEmpty>
        <cms:ifEmpty nodeDataName="title"><cms:out nodeDataName="name"/></cms:ifEmpty>
      </title>
      <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/samples.css" />
      <cms:links/>
  </head>
  <body>

    <!-- Page Dialog  -->
    <cms:mainBar dialog="mainProperties"/>

    <div id="header">
        <h1>Magnolia Samples</h1>
    </div>

    <!-- Simple Navigation Tag  -->
    <div id="navigation"><cmsu:simpleNavigation/></div>

    <div id="main">
        <!-- Variable titleSize is set in the template definition, if empty sets it to 1 -->

        <h${def.titleSize}>
            <cms:ifNotEmpty nodeDataName="title">${content.title}</cms:ifNotEmpty>
            <cms:ifEmpty nodeDataName="title"><cms:out nodeDataName="name"/></cms:ifEmpty>
        </h${def.titleSize}><br />
        <div>
            <p>${content.text}</p><br />
        </div>
        <br />

        <!-- Variable dummy is set in the template definition parameters content node -->
        <p>dummy: ${def.parameters.dummy}</p><br />

        <div>
         <h3>Display Sources</h3><br />
         <ul>
             <li><a href="${pageContext.request.contextPath}/.sources/templates/main.jsp">Main template</a></li>
         </ul>
        </div><br />

        <cms:contentNodeIterator contentNodeCollectionName="main">
            <cms:includeTemplate/>
        </cms:contentNodeIterator>

        <cms:newBar contentNodeCollectionName="main" paragraph="samplesHowToJSP, samplesHowToFTL, samplesControlsShowRoom, samplesSearchResult"/>

    </div>

    <div id="footer">
        <p>This page was last edited by <span class="author"><cms:out nodeDataName="mgnl:authorid" contentNodeName="MetaData"/></span>
            on <span class="modificationdate"><cms:out nodeDataName="mgnl:lastmodified" contentNodeName="MetaData"/></span></p>
    </div>

  </body>
</html>
