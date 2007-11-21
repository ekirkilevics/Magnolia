<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" />
    </head>
    <body>
      <cms:mainBar paragraph="${module.paragraphs.page}" />
      <div id="contentDivMainColumn">
        <c:import url="/templates/samples/templates/inc/columnMain.jsp" />
        <form name="mgnlsearch" action="" method="post">
          <input id="query" name="query" value="${fn:escapeXml(param.query)}" />
          <input type="submit" name="search" value="search" />
        </form>
        <c:if test="${!empty(param.query)}">
          <h1>Search results for:</h1>
          <h2>${fn:escapeXml(param.query)}</h2>
          <cmsu:simpleSearch query="${param.query}" var="results" />
          <c:if test="${empty(results)}">
            <p>No results</p>
          </c:if>
          <c:forEach var="node" items="${results}">
            <div class="searchresult">
              <h4>${node.title}</h4>
              <p>
                <cmsu:searchResultSnippet query="${param.query}" page="${node}" />
              </p>
              <a href="${pageContext.request.contextPath}${node.handle}.html">
                ${pageContext.request.contextPath}${node.handle}.html
              </a>
              <em>
                last modification date:
                <fmt:formatDate dateStyle="full" value="${node.metaData.modificationDate.time}" />
              </em>
            </div>
          </c:forEach>
        </c:if>
        <div id="footer">
          <cms:adminOnly>
            <fmt:message key="buttons.editfooter" var="label" />
            <cms:editButton label="${label}" paragraph="${module.paragraphs.footer}" contentNodeName="footerPar" />
          </cms:adminOnly>
          <cms:ifNotEmpty nodeDataName="footerText" contentNodeName="footerPar">
            <p>
              <cms:out nodeDataName="footerText" contentNodeName="footerPar" />
            </p>
          </cms:ifNotEmpty>
          <a href="http://www.magnolia.info">
            <img src="${pageContext.request.contextPath}/docroot/samples/imgs/poweredSmall.gif"
              alt="Powered by Magnolia" />
          </a>
        </div>
      </div>
      <div id="contentDivRightColumn">
        <cms:contentNodeIterator contentNodeCollectionName="rightColumnParagraphs">
          <div style="clear:both;">
            <cms:editBar adminOnly="true" />
            <cms:includeTemplate />
          </div>
        </cms:contentNodeIterator>
        <cms:adminOnly>
          <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="rightColumnParagraphs" paragraph="${module.paragraphs.rightColumn}" />
          </div>
        </cms:adminOnly>
      </div>
      <div style="position:absolute;left:0px;top:0px;">
        <cmsu:img nodeDataName="headerImage" inherit="true" />
      </div>
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>
