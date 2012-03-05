<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>
<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn"%>


<h3>
  <c:choose>
    <c:when test="${not empty content.title}">
      ${content.title}
    </c:when>
    <c:otherwise>
      ${content['@name']}
    </c:otherwise>
  </c:choose>
</h3>

Text:
  <c:choose>
    <c:when test="${not empty content.text}">
      ${cmsfn:decode(content).text}
    </c:when>
    <c:otherwise>
      No text defined yet
    </c:otherwise>
  </c:choose>

<p>
  <c:choose>
    <c:when test="${not empty content.image}">
      Image: <img src="${cmsfn:link(content.image)}" />
    </c:when>
    <c:otherwise>
      Image: No image uploaded yet.
    </c:otherwise>
  </c:choose>
</p>

<p>
  <c:choose>
    <c:when test="${not empty content.date}">
      <fmt:formatDate value="${content.date.time}" var="parsedDate" pattern="EEEE, d. MMMM yyyy" />
        The date you specified: <c:out value="${parsedDate}" />
    </c:when>
    <c:otherwise>
      No date specified yet.
    </c:otherwise>
  </c:choose>
</p>

<h4>New Search</h4>
<%@include file="/templates/samples/includes/searchForm.jsp" %>


<h4>Display Component's Sources</h4>
<ul>
    <li><a href="${pageContext.request.contextPath}/.sources/components/howTo.jsp">'HowTo' component</a></li>
</ul>