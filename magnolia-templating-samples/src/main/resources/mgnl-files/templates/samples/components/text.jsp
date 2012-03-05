<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>
<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn"%>

<c:if test="${not empty content.title}">
  <h3>${content.title}</h3>
</c:if>

<c:if test="${not empty content.text}">
  ${cmsfn:decode(content).text}
</c:if>
