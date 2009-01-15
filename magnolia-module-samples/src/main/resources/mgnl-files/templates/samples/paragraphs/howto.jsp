<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<cms:editBar />
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
<li>get from request: ${sampleFilter}</li>

</ul>
</div>
<div id="search" >
    <form name="mgnlsearch" action="${ctx.contextPath}/SearchResult.html" method="post">
      <input id="query" name="query" value="${query}" />
      <input type="submit" name="search" value="search" />
    </form>
</div><!-- end search -->
<div>
<h3>Display Paragraph Sources</h3>
<ul>
<li><a href="${pageContext.request.contextPath}/.sources/paragraphs/sample.jsp">Sample paragraph</a></li>
</ul>
</div>

