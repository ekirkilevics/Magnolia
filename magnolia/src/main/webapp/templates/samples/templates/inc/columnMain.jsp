<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
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
</jsp:root>
