<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
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
