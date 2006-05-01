<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:ifNotEmpty nodeDataName="image">
    <cms:setNode var="imagedata" />
    <cms:out nodeDataName="image" var="imageurl" />
    <img src="${pageContext.request.contextPath}${imageurl}" class="contentImage_${imagedata.imageFloat}"
      alt="${imagedata.imageAlt}" />
  </cms:ifNotEmpty>
  <cms:ifNotEmpty nodeDataName="title">
    <h2>
      <cms:out nodeDataName="title" />
    </h2>
  </cms:ifNotEmpty>
  <cms:out nodeDataName="text" />
</jsp:root>
