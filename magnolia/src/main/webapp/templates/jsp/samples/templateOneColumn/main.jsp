<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=utf-8" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <!-- exposes the current node for use with jstl -->
      <cms:setNode var="pageProperties" />
      <title>Magnolia 2.1 Samples | ${pageProperties.title}</title>
      <!--  add magnolia css and js links -->
      <cms:links />
      <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/main.css" />
      <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/docroot/samples/css/richEdit.css" />
      <script type="text/javascript" src="${pageContext.request.contextPath}/docroot/samples/js/form.js">
        <jsp:text><!--  --></jsp:text>
      </script>
      <meta name="description" content="${pageProperties.metaDescription}" />
      <meta name="keywords" content="${pageProperties.metaKeywords}" />
    </head>
    <body>
      <div id="contentDivMainColumnTotalWidth">
        <!-- content title -->
        <c:set var="title">
          <cms:out nodeDataName="title" />
        </c:set>
        <c:if test="%{empty(title)}">
          <c:set var="title">
            <cms:out nodeDataName="contentTitle" />
          </c:set>
        </c:if>
        <h1>${title}</h1>
        <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
          <c:set var="spacer">
            <cms:out nodeDataName="spacer" />
          </c:set>
          <c:set var="lineAbove">
            <cms:out nodeDataName="lineAbove" />
          </c:set>
          <div style="clear:both;">
            <cms:adminOnly>
              <cms:editBar />
            </cms:adminOnly>
            <!-- line -->
            <c:if test="${lineAbove=='true'}">
              <div class="line">
                <br />
              </div>
            </c:if>
            <cms:includeTemplate />
          </div>
          <!-- spacer -->
          <div style="clear:both;">
            <c:if test="${spacer=='1'}">
              <br />
            </c:if>
            <c:if test="${spacer=='2'}">
              <br />
              <br />
            </c:if>
          </div>
        </cms:contentNodeIterator>
        <cms:adminOnly>
          <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="mainColumnParagraphs"
              paragraph="samplesTextImage,samplesEditor,samplesDownload,samplesLink,samplesTable" />
          </div>
        </cms:adminOnly>
        <br />
        <br />
        <br />
        <div class="line">
          <br />
        </div>
        <cms:adminOnly>
          <fmt:message key="buttons.editfooter" var="label" />
          <cms:editButton label="${label}" paragraph="samplesPageFooter" contentNodeName="footerPar" />
          <br />
        </cms:adminOnly>
        <cms:ifNotEmpty nodeDataName="footerText" contentNodeName="footerPar">
          <cms:out nodeDataName="footerText" contentNodeName="footerPar" />
          <br />
          <br />
        </cms:ifNotEmpty>
        <a href="http://www.magnolia.info" target="_blank">
          <img align="right" border="0" style="margin-top: 5px"
            src="${pageContext.request.contextPath}/docroot/samples/imgs/poweredSmall.gif" />
        </a>
      </div>
      <c:import url="/templates/jsp/samples/global/headerImage.jsp" />
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>
