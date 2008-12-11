<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>

<cms:editBar editLabel="I want to edit my paragraph" moveLabel="" deleteLabel=""/>
<h3><cms:out nodeDataName="title" /></h3>
<div>

Image: <img src="${pageContext.request.contextPath}<cms:out nodeDataName="image" />" />
<br />
Text: <cms:out nodeDataName="text" />
<br />
The date you spedified: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" />
</div>

