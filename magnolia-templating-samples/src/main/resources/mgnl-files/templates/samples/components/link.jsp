<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>


<c:catch  var="exception">
   <c:set var="target" scope="request" value="${model.target}" />
</c:catch>

<c:if test="${empty exception}">
    <li>
        <a href="${model.targetLink}">
          <c:choose>
            <c:when test="${not empty content.title}">
              ${content.title}
            </c:when>
            <c:when test="${empty content.title and not empty target.title}">
              ${target.title}
            </c:when>
            <c:otherwise>
              ${target['@name']}
            </c:otherwise>
          </c:choose>
        </a>
    </li>
</c:if>
<c:if test="${not empty exception}">
    <li>
        <a href="${content.target}">
          <c:choose>
        <c:when test="${not empty content.title}">
              ${content.title}
            </c:when>
            <c:otherwise>
              ${content.target}
            </c:otherwise>
          </c:choose>
        </a>
    </li>
</c:if>