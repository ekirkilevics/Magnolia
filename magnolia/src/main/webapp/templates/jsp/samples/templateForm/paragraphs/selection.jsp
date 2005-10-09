<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">

    <cms:setNode var="par"/>
    <jsp:scriptlet>
        pageContext.setAttribute("newline", "\n");
    </jsp:scriptlet>

    <div class="formelement">

      <c:if test="${!empty(par.title)}">
         <label for="${par.name}">
           ${par.title}
           <c:if test="${par.mandatory}">*</c:if>
           <input type="hidden" name="mgnlMandatory" value="${par.name}" />
         </label>
      </c:if>


      <c:choose>
        <c:when test="${par.type eq 'select'}">
          <select name="${par.name}" id="${par.name}">
            <c:forTokens items="${par.values}" var="option" delims="${newline}">
              <option value="${option}">${option}</option>
            </c:forTokens>
          </select>
        </c:when>
        <c:otherwise>
            <c:forTokens items="${par.values}" var="option" delims="${newline}">
              <input name="${par.name}" type="${par.type}" class="${par.type}" value="${option}" />${option}<br/>
            </c:forTokens>
        </c:otherwise>
      </c:choose>
  </div>

</jsp:root>
