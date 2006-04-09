<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
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
</jsp:root>
