<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<jsp:directive.page import="info.magnolia.context.MgnlContext" />
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
<li>get from request: <jsp:scriptlet>out.print(MgnlContext.getAttribute("sampleFilter"));</jsp:scriptlet></li>

</ul>
</div>
<div id="search" >
    <form name="mgnlsearch" action="" method="post">
      <input type="hidden" id="resultPage" name="resultPage" value="searchResultJSP" />
      <input id="query" name="query" value="${query}" />
      <input type="submit" name="search" value="search" />
    </form>
</div><!-- end search -->
<div>
<h3>Display Sources</h3>
<ul>
<li><a href="${pageContext.request.contextPath}/.sources/templates/main-sample.jsp">Main template</a></li>
<li><a href="${pageContext.request.contextPath}/.sources/templates/paragraphs/main-sample-jsp.jsp">Main paragraph</a></li>
<li><a href="${pageContext.request.contextPath}/.sources/templates/paragraphs/search-jsp.jsp">Search paragraph</a></li>
<li><a href="${pageContext.request.contextPath}/.sources/templates/include/footer.jsp">Footer</a></li>
<li><a href="${pageContext.request.contextPath}/.sources/templates/include/header.jsp">Header</a></li>
</ul>
</div>

