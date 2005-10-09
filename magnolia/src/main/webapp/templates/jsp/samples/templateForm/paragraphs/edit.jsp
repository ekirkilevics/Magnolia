<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">

    <cms:setNode var="par"/>
    <div class="formelement">

      <c:if test="${!empty(par.title)}">
         <label for="${par.name}">
           ${par.title}
           <c:if test="${par.mandatory}">*</c:if>
           <input type="hidden" name="mgnlMandatory" value="${par.name}" />
         </label>
      </c:if>

      <c:choose>
        <c:when test="${par.rows eq 1}">
          <input type="text" name="${par.name}" id="${par.name}" class="text" /><br/>
        </c:when>
        <c:otherwise>
          <textarea name="${par.name}" id="${par.name}" rows="${par.rows}"><!--  --></textarea>
        </c:otherwise>
      </c:choose>

    </div>

</jsp:root>