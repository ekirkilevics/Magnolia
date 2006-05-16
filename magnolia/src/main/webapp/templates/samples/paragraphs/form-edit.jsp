<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:setNode var="par" />
  <div class="formelement">
    <c:if test="${!empty(par.title)}">
      <label for="${contentObj.name}">
        ${par.title}
        <c:if test="${par.mandatory}">*
            <input type="hidden" name="mgnlMandatory" value="${contentObj.name}" />
        </c:if>
      </label>
    </c:if>
    <c:choose>
      <c:when test="${par.rows eq 1}">
        <input type="text" name="${contentObj.name}" class="text" />
        <br />
      </c:when>
      <c:otherwise>
        <textarea name="${contentObj.name}" rows="${par.rows}"><!--  --></textarea>
      </c:otherwise>
    </c:choose>
  </div>
</jsp:root>