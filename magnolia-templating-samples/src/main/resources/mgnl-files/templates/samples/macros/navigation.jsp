<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn"%>


<!--  Set Display Name -->
<c:choose>
	<c:when test="${not empty pageNode.title}">
		<c:set var="displayName" value="${pageNode.title}"/>
	</c:when>
	<c:otherwise>
		<c:set var="displayName" value="${pageNode['@name']}"/>
	</c:otherwise>
</c:choose>



<!-- Get the currentPage -->
<c:set var="currentPage" value="${cmsfn:page(content)}"/>

<!-- Check if the current page is selected -->
<c:set var="selected" value="${pageNode['@path'] == currentPage['@path']}"/>


<c:choose>
	<c:when test="${selected == true}">
		<li class="selected">
       		<span>${displayName}</span>
    	</li>
	</c:when>
	<c:otherwise>
		<li>
       		<a href="${cmsfn:link(pageNode)}"><span>${displayName}</span></a>
    	</li>
	</c:otherwise>
</c:choose>

<!-- Check maxDepth -->

<c:if test="${pageNode['@depth'] < maxDepth}">
   <c:forEach var="pageNodeSubElement" begin="0" items="${cmsfn:children(pageNode, 'mgnl:page')}">
     <c:set var="pageNode" scope="request" value="${pageNodeSubElement}" />
     <jsp:include page="/templates/samples/macros/navigation.jsp" />
   </c:forEach>
</c:if>
