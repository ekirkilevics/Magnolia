<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<jsp:directive.page import="info.magnolia.context.MgnlContext" />
<cms:editBar editLabel="I want to edit my paragraph" moveLabel="" deleteLabel=""/>
<h3><cms:out nodeDataName="title" /></h3>
<div>
<cms:out nodeDataName="image" var="imageurl" />
Image: <img src="${pageContext.request.contextPath}${imageurl}" />
<br />
Text: <cms:out nodeDataName="text" />
<br />
The date you spedified: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" />
<br />
<ul>Parameter set by filter:
<li>using model: ${model.filterAttribute} </li>
<li>get from request: <jsp:scriptlet>out.print(MgnlContext.getAttribute("sampleFilter"));</jsp:scriptlet></li>

</ul>
</div>

