<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>


<div id="footer">


  <c:forEach items="${components}" var="component">
      <div id="footer-element">
            <cms:component content="${component}" />
        </div>
  </c:forEach>

</div>