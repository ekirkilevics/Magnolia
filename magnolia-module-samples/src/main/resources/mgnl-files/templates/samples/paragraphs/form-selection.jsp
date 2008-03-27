<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:setNode var="par" />
  <jsp:scriptlet>pageContext.setAttribute("newline", "\r\n");</jsp:scriptlet>
  <div class="formelement">
    <c:if test="${!empty(par.title)}">
      <label for="${contentObj.name}">
        ${par.title}
        <c:if test="${par.mandatory}">*
            <input type="hidden" name="mgnlMandatory" value="field_${contentObj.name}" />
        </c:if>
      </label>
    </c:if>
    <c:choose>
      <c:when test="${par.type eq 'select'}">
        <select name="field_${contentObj.name}">
          <c:forTokens items="${par.values}" var="option" delims="${newline}">
            <option value="${option}">${option}</option>
          </c:forTokens>
        </select>
      </c:when>
      <c:otherwise>
        <div class="optiongroup">
          <c:forTokens items="${par.values}" var="option" delims="${newline}">
            <input name="field_${contentObj.name}" type="${par.type}" class="${par.type}" value="${option}" />
            ${option}
            <br />
          </c:forTokens>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</jsp:root>