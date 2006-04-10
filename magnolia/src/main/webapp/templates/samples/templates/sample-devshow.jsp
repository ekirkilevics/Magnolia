<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" />
    </head>
    <body>
      <cms:mainBar paragraph="samplesPageProperties" />
      <div id="contentDivMainColumnTotalWidth">
        <!-- content title -->
        <h1>
          <cms:ifNotEmpty nodeDataName="contentTitle" actpage="true">
            <cms:out nodeDataName="contentTitle" />
          </cms:ifNotEmpty>
          <cms:ifEmpty nodeDataName="contentTitle" actpage="true">
            <cms:out nodeDataName="title" />
          </cms:ifEmpty>
        </h1>
        <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphsDev">
          <div style="clear:both;">
            <cms:editBar adminOnly="true" />
            <cms:includeTemplate />
            <br />
            <br />
          </div>
        </cms:contentNodeIterator>
        <cms:adminOnly>
          <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="mainColumnParagraphsDev"
              paragraph="samplesDevShowRichEdit,samplesDevShowDate,samplesDevShowFile,samplesDevShowAllControls,samplesDevShowInclude" />
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
      <c:import url="/templates/samples/templates/inc/headerImage.jsp" />
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>