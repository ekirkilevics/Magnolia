<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>

<cms:edit />

<c:if test="${not empty content.title}">
  <h3>${content.title}</h3>
</c:if>

<c:if test="${not empty content.text}">
  ${content.text}
</c:if>
