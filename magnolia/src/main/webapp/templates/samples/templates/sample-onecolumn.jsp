<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
  xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
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
      <cms:mainBar paragraph="samplesPageProperties" />
      <div id="contentDivMainColumnTotalWidth">
        <!-- content title -->
        <cms:out nodeDataName="title" var="title" />
        <c:if test="%{empty(title)}">
          <cms:out nodeDataName="contentTitle" var="title" />
        </c:if>
        <h1>${title}</h1>
        <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
          <cms:out nodeDataName="lineAbove" var="lineAbove" />
          <div style="clear:both;">
            <cms:editBar adminOnly="true" />
            <!-- line -->
            <c:if test="${lineAbove=='true'}">
              <hr />
            </c:if>
            <cms:includeTemplate />
          </div>
          <!-- spacer -->
          <cms:out nodeDataName="spacer" var="spacer" />
          <div style="clear:both;" class="spacer${spacer}">
            <!--  -->
          </div>
        </cms:contentNodeIterator>
        <cms:adminOnly>
          <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="mainColumnParagraphs"
              paragraph="samplesTextImage,samplesEditor,samplesDownload,samplesLink,samplesTable" />
          </div>
        </cms:adminOnly>
        <div id="footer">
          <cms:adminOnly>
            <fmt:message key="buttons.editfooter" var="label" />
            <cms:editButton label="${label}" paragraph="samplesPageFooter" contentNodeName="footerPar" />
          </cms:adminOnly>
          <cms:ifNotEmpty nodeDataName="footerText" contentNodeName="footerPar">
            <p>
              <cms:out nodeDataName="footerText" contentNodeName="footerPar" />
            </p>
          </cms:ifNotEmpty>
          <a href="http://www.magnolia.info">
            <img src="${pageContext.request.contextPath}/docroot/samples/imgs/poweredSmall.gif" />
          </a>
        </div>
      </div>
      <div style="position:absolute;left:0px;top:0px;">
        <cms:out var="headerImage" nodeDataName="headerImage" inherit="true" />
        <cms:out var="headerImageAlt" nodeDataName="headerImageAlt" inherit="true" />
        <c:if test="${empty(headerImage)}">
          <c:set var="headerImage" value="/docroot/samples/imgs/header.jpg" />
          <c:set var="headerImageAlt" value="Magnolia - for content management" />
        </c:if>
        <img src="${pageContext.request.contextPath}${headerImage}" alt="${headerImageAlt}" />
      </div>
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>
