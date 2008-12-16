<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="search-results" >
    <p>Query Results for <b>${param.query}</b></p>
    <c:if test="${!empty(param.query)}">
          <cmsu:simpleSearch query="${param.query}" var="results" />
          <c:if test="${empty(results)}">
            <p>No results</p>
          </c:if>
          <c:forEach var="node" items="${results}">
            <div class="searchresult">
              <h4>${node.title}</h4>
              <p>
                <cmsu:searchResultSnippet query="${param.query}" page="${node}" />
              </p>
              <a href="${pageContext.request.contextPath}${node.handle}.html">
                ${pageContext.request.contextPath}${node.handle}.html
              </a>

            </div>
          </c:forEach>
        </c:if>

</div><!-- end search -->