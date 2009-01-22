<!-- -------------- HowTo Paragraph -------------- -->

<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<cms:editBar />
<h3><cms:ifNotEmpty nodeDataName="title">${content.title}</cms:ifNotEmpty>
    <cms:ifEmpty nodeDataName="title"><cms:out nodeDataName="name"/></cms:ifEmpty></h3>
<div>
  <cms:out nodeDataName="image" var="imageurl" />
  Image: <img src="${pageContext.request.contextPath}${imageurl}" />
  <br />
  <div>
    Text: <cms:out nodeDataName="text" />
  </div>
  <br />
  The date you spedified: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" />
  <br />
  <ul>Parameter set by filter:
    <li>using model: ${model.filterAttribute} </li>
    <li>get from request: ${sampleFilter}</li>
  </ul>
</div>
<div id="search" >
    <form name="mgnlsearch" action="${pageContext.request.contextPath}/searchResult.html" method="post">
      <input id="query" name="query" value="${query}" />
      <input type="submit" name="search" value="search" />
    </form>
    <br />
</div><!-- end search -->
<div>
  <h3>Display Paragraph Sources</h3>
  <ul>
     <li><a href="${pageContext.request.contextPath}/.sources/paragraphs/howTo.jsp">HowTo paragraph</a></li>
  </ul>
</div><br />
