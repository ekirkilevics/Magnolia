<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:ifNotEmpty nodeDataName="title">
    <div class="rightColumnTitle">
      <cms:out nodeDataName="title" />
    </div>
  </cms:ifNotEmpty>
  <cms:out nodeDataName="text" />
  <div class="line">
    <br />
  </div>
</jsp:root>
