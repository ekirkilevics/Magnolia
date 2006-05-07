<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
  xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:setNode var="pageProperties" />
  <c:if test="${!empty(param.sendmail)}">
    <cmsu:simpleMail to="${pageProperties.to}" from="${pageProperties.from}" cc="${pageProperties.cc}"
      bcc="${pageProperties.cc}" redirect="${pageProperties.redirect}" subject="${pageProperties.subject}"
      nodeCollectionName="mainColumnParagraphs" />
  </c:if>
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" />
      <script type="text/javascript" src="${pageContext.request.contextPath}/docroot/samples/js/form.js">
        <!--  -->
      </script>
    </head>
    <body>
      <cms:mainBar paragraph="samplesPageProperties" label="Page Properties">
        <cms:button label="Form properties" dialogName="samplesFormProperties" position="right" />
      </cms:mainBar>
      <div id="contentDivMainColumn">
        <c:set var="alertText">
          <c:out escapeXml="true" value="${pageProperties.mandatoryAlert}" />
        </c:set>
        <c:if test="${empty(alertText)}">
          <c:set var="alertText">Please fill in all fields marked with an asterisk.</c:set>
        </c:if>
        <form name="samplesForm" action="${pageContext.request.contextPath}${actpage.handle}.html" method="post"
          onsubmit="return (checkMandatories(this.name,'${alertText}'));">
          <input type="hidden" name="sendmail" value="true" />
          <!-- content title -->
          <cms:out nodeDataName="title" var="title" />
          <c:if test="${empty(title)}">
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
          <!-- new bar -->
          <cms:adminOnly>
            <div style="clear:both;">
              <cms:newBar contentNodeCollectionName="mainColumnParagraphs"
                paragraph="samplesTextImage,samplesFormEdit,samplesFormSelection,samplesFormSubmit" />
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
              <img src="${pageContext.request.contextPath}/docroot/samples/imgs/poweredSmall.gif"
                alt="Powered by Magnolia" />
            </a>
          </div>
        </form>
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
