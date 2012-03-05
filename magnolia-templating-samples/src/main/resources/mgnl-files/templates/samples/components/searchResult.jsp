<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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


<c:if test="${not empty content.text}">
    <p>
       ${cmsfn:decode(content).text}
    </p>
</c:if>


<c:if test="${not empty model.searchResult}">
    <div id="search-results" >
        <h3>Query Results for:
      <c:choose>
        <c:when test="${not empty model.query}">
          ${model.query}
        </c:when>
        <c:otherwise>
          No query set yet
        </c:otherwise>
      </c:choose>
        </h3>

        <ul>
          <c:forEach items="${model.searchResult}" var="resultItem">
        <c:set var="foundOnPage" value="${cmsfn:page(resultItem)}"/>
        <li>
          <a href="${pageContext.request.contextPath}${foundOnPage['@path']}">
            <c:choose>
              <c:when test="${not empty foundOnPage.title}">
                ${foundOnPage.title}
              </c:when>
              <c:otherwise>
                ${foundOnPage['@name']}
              </c:otherwise>
            </c:choose>
          </a>
          <span>(found in Node: ${resultItem['@path']})</span>
        </li>
      </c:forEach>
        </ul>
    </div><!-- end search-results -->
</c:if>

<h4>New Search</h4>
<jsp:include page="/templates/samples/includes/searchForm.jsp" />



<h4>Display Component Sources</h4>
<ul>
    <li><a href="${pageContext.request.contextPath}/.sources/components/searchResult.jsp">SearchResult component</a></li>
</ul>

