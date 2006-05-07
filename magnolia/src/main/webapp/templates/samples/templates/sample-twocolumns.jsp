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
      <c:import url="/templates/samples/templates/inc/head.jsp" />
    </head>
    <body>
      <cms:mainBar paragraph="samplesPageProperties" />
      <div id="contentDivMainColumn">
        <c:import url="/templates/samples/templates/inc/columnMain.jsp" />
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
            <cms:newBar contentNodeCollectionName="rightColumnParagraphs" paragraph="samplesRightColumn" />
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