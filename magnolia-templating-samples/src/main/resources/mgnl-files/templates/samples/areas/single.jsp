<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>

<div id="${def.parameters.divId}">


  <c:if test="${not empty component}">
       <cms:component content="${component}"/>
    </c:if>

</div>