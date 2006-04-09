<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=utf-8" />
  <jsp:directive.page import="info.magnolia.cms.util.Resource" />
  <jsp:directive.page import="info.magnolia.cms.core.Content" />
  <jsp:directive.page import="info.magnolia.cms.gui.inline.BarMain" />
  <jsp:text>
    <![CDATA[
        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/jsp/samples/global/head.jsp" />
    </head>
    <body>
      <jsp:scriptlet>
        <![CDATA[

        if (request.getParameter("mail") == null) {
            Content currentPage = Resource.getActivePage(request);

            BarMain bar = new BarMain(request);

            //path is needed for the links of the buttons
            bar.setPath(currentPage.getHandle());

            //"paragraph" specifies the paragraph evoked by the "Properties" button
            bar.setParagraph("samplesPageProperties");

            // initialize the default buttons (preview, site admin, properties)
            // note: buttons are not placed through init (see below)
            bar.setDefaultButtons();


            bar.placeDefaultButtons();

            //draw the main bar
            bar.drawHtml(out);
        }
        ]]>
      </jsp:scriptlet>
      <br />
      <br />
      <jsp:scriptlet>
        <![CDATA[
        // prepare page title
        String title = Resource.getActivePage(request).getNodeData("title").getString();
        if (title == null) {
          title = Resource.getActivePage(request).getNodeData("contentTitle").getString();
        }
        title = title.toUpperCase();
        // make it accessable for jstl
        pageContext.setAttribute("title", title);
        ]]>
      </jsp:scriptlet>
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
          <cms:newBar contentNodeCollectionName="mainColumnParagraphs" paragraph="samplesTextImage" />
        </div>
      </cms:adminOnly>
    </body>
  </html>
</jsp:root>