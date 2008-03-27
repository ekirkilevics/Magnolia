<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:setNode var="par" />
  <c:set var="blank" value=" " />
  <c:set var="cssclass">
    data ${blank}
    <c:if test="${par.tableAltBg}">table-alt ${blank}</c:if>
    <c:if test="${par.tableLinesHorizontal}">table-linesh ${blank}</c:if>
    <c:if test="${par.tableLinesVertical}">table-linesv ${blank}</c:if>
    <c:if test="${par.tableFontSmall}">table-fontsm ${blank}</c:if>
    <c:if test="${par.tableAlignment}">table-alignright</c:if>
  </c:set>
  <div class="paragraph">
    <h3 class="titleunderline">${par.title}</h3>
    <cmsu:table header="${par.tableHeader}" class="${cssclass}">${par.tableData}</cmsu:table>
  </div>
</jsp:root>
