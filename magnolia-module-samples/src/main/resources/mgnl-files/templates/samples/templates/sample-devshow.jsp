<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" charEncoding="UTF-8"/>
    </head>
    <body>
      <cms:mainBar paragraph="${module.paragraphs.page}" />
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
              paragraph="${module.paragraphs.devShow}" />
          </div>
        </cms:adminOnly>
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
      <div style="position:absolute;left:0px;top:0px;">
        <cmsu:img nodeDataName="headerImage" inherit="true" />
      </div>
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>