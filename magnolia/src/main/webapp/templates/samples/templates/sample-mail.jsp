<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <jsp:text>
    <![CDATA[
        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" />
    </head>
    <body>
      <c:if test="${empty(param.mail)}">
        <cms:mainBar paragraph="samplesPageProperties" label="Page Properties" />
      </c:if>
      <br />
      <br />
      <cms:out nodeDataName="title" var="title" />
      <c:if test="${empty(title)}">
        <cms:out nodeDataName="contentTitle" var="title" />
      </c:if>
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
          <cms:newBar contentNodeCollectionName="mainColumnParagraphs" paragraph="samplesTextImage" />
        </div>
      </cms:adminOnly>
    </body>
  </html>
</jsp:root>