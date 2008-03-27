<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <div>
    <cms:out nodeDataName="document" var="document" />
    <cms:out nodeDataName="document" fileProperty="extensionUpperCase" var="extension" />
    <cms:out nodeDataName="document" fileProperty="size" var="size" />
    <c:set var="icon">
      <cms:out nodeDataName="document" fileProperty="icon" />
    </c:set>
    <a href="${pageContext.request.contextPath}${document}" target="_blank">
      <img src="${pageContext.request.contextPath}${icon}" alt="${extension}"
        style="border: none; display:inline; vertical-align: bottom;" />
      <cms:ifEmpty nodeDataName="text">
        <cms:out nodeDataName="document" fileProperty="nameWithoutExtension" />
      </cms:ifEmpty>
      <cms:ifNotEmpty nodeDataName="text">
        <cms:out nodeDataName="text" />
      </cms:ifNotEmpty>
    </a>
    &amp;ndash; ${extension}-File, ${size}
  </div>
</jsp:root>
