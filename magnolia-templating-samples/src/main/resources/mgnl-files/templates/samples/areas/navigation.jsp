<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn"%>

<%@include file="/templates/samples/includes/searchForm.jsp" %>


<div id="navigation">

    <c:set var="pageNode" scope="request" value="${cmsfn:root(content, 'mgnl:page')}" />
    <c:set var="maxDepth" scope="request" value="2" />
    <ul>
       <jsp:include page="/templates/samples/macros/navigation.jsp" />
    </ul>

</div><!-- end "navigation" -->