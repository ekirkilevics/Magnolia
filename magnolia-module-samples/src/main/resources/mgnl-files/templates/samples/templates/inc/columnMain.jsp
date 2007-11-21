<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
<jsp:directive.page import="java.util.Collection"/>
<jsp:directive.page import="org.apache.commons.lang.StringUtils"/>
<jsp:directive.page import="info.magnolia.module.samples.SamplesConfig"/>
<jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <!-- content title -->
  <cms:out nodeDataName="title" var="title" />
  <c:if test="%{empty(title)}">
    <cms:out nodeDataName="contentTitle" var="title" />
  </c:if>
  <h1>${title}</h1>
  
  <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
    <cms:out nodeDataName="lineAbove" var="lineAbove" />
    <cms:out nodeDataName="spacer" var="spacer" />
    <div style="clear:both;" class="spacer${spacer}">
      <cms:editBar adminOnly="true" />
      <!-- line -->
      <c:if test="${lineAbove=='true'}">
        <hr />
      </c:if>
      <cms:includeTemplate />
    </div>
  </cms:contentNodeIterator>
  <cms:adminOnly>
    <div style="clear:both;">
      <cms:newBar contentNodeCollectionName="mainColumnParagraphs" paragraph="${module.paragraphs['mainColumn']}" /> 
    </div>
  </cms:adminOnly>
</jsp:root>
