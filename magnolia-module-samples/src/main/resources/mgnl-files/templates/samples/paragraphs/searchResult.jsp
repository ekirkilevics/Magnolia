<!-- -------------- SearchResult Paragraph -------------- -->

<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<cms:editBar />
<h3><cms:ifNotEmpty nodeDataName="title">${content.title}</cms:ifNotEmpty>
    <cms:ifEmpty nodeDataName="title"><cms:out nodeDataName="name"/></cms:ifEmpty></h3>
<br />

<p>${content.text}</p>

<br />
<div id="search-results" >
    <h4>Query Results for ${param.query}</h4>
    <br />
    <c:if test="${!empty(param.query)}">
          <cmsu:simpleSearch query="${param.query}" var="results" />
          <c:if test="${empty(results)}">
            <p>No results</p>
          </c:if>
          <ul>
          <c:forEach var="node" items="${results}">
            <div class="searchresult">
              <li>
                <h4>${node.title}</h4>
                <a href="${pageContext.request.contextPath}${node.handle}.html">
                  ${node.title}
                </a>
            </li>
            </div>
          </c:forEach>
          </ul>
        </c:if>
</div><!-- end search -->
<br />
<h4>New Search</h4>
<div id="search" >
    <form name="mgnlsearch" action="${pageContext.request.contextPath}/SearchResult.html" method="post">
      <input id="query" name="query" value="${query}" />
      <input type="submit" name="search" value="search" />
    </form>
    <br />
</div><!-- end search -->
<br />
<div>
  <h3>Display Paragraph Sources</h3>
  <ul>
     <li><a href="${pageContext.request.contextPath}/.sources/paragraphs/searchResult.jsp">HowTo paragraph</a></li>
  </ul>
</div><br />
