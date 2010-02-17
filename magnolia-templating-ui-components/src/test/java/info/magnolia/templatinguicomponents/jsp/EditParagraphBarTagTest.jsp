<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/authoringui" %>

${pageContext.getAttribute("content")}
${requestScope.getAttribute("content")}
<%=request.getAttribute("content")%>
<ui:edit editLabel="Edit this !" move="true" delete="false" dialog="myDialog"/>

<!-- TODO try to set target too -->