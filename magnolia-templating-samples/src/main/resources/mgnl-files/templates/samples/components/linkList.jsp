<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>


<c:if test="${not empty content.title}">
   <h3>${content.title}</h3>
</c:if>
<c:if test="${not empty content.text}">
   <h4>${content.text}</h4>
</c:if>
<div class="linkList">
    <ul>
        <cms:area name="links" />
    </ul>
</div>