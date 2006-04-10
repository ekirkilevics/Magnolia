<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page import="info.magnolia.cms.beans.config.Server" />
  <jsp:directive.page import="info.magnolia.cms.core.Content" />
  <jsp:directive.page import="info.magnolia.cms.mail.MailConstants" />
  <jsp:directive.page import="info.magnolia.cms.mail.MgnlMailFactory" />
  <jsp:directive.page import="info.magnolia.cms.mail.templates.MgnlEmail" />
  <jsp:directive.page import="info.magnolia.cms.util.Resource" />
  <jsp:directive.page import="java.util.Iterator" />
  <jsp:directive.page import="info.magnolia.cms.core.Content" />
  <jsp:directive.page import="info.magnolia.cms.util.Resource" />
  <jsp:directive.page import="info.magnolia.cms.gui.inline.BarMain" />
  <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />
  <jsp:scriptlet>
    <![CDATA[ 
    if (request.getParameter("sendMail") != null) { 
       StringBuffer body = new StringBuffer(); //build and send email 
       Iterator it = Resource.getActivePage(request).getContent("mainColumnParagraphs").getChildren().iterator(); 
       
       while (it.hasNext()) {
         Content node = (Content) it.next(); 
         if (request.getParameterValues(node.getName()) != null) {
           String[] values = request.getParameterValues(node.getName()); 
           body.append(node.getNodeData("title").getString() + "\n"); 
           for (int i = 0; i < values.length; i++) {
             body.append(values[i] + "\n"); } body.append("\n\n"); }
           }
           String subject = Resource.getActivePage(request).getNodeData("subject").getString();
           String from = Resource.getActivePage(request).getNodeData("from").getString();
           String to = Resource.getActivePage(request).getNodeData("to").getString();
           String cc = Resource.getActivePage(request).getNodeData("cc").getString();
           String server = Resource.getActivePage(request).getNodeData("server").getString();
           
           if (server.equals("")) {
             server = Server.getDefaultMailServer();
           }
           
           String redirect = Resource.getActivePage(request).getNodeData("redirect").getString();
           
           try {
             MgnlEmail email = MgnlMailFactory.getInstance().getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
             email.setToList(to);
             email.setFrom(from);
             email.setSubject(subject);
             email.setBody(body.toString(), null);
             MgnlMailFactory.getInstance().getEmailHandler().prepareAndSendMail(email);
           } catch (Exception e) {
             // you may want to warn the user redirecting him to a different page... 
           }
           
           response.sendRedirect(request.getContextPath() + redirect); 
        }
  ]]>
  </jsp:scriptlet>
  <jsp:directive.page contentType="text/html; charset=utf-8" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" />
    </head>
    <body>
      <!-- main bar of form template -->
      <jsp:scriptlet>
        <![CDATA[
    Content currentPage=Resource.getActivePage(request);

    BarMain bar=new BarMain(request);

    //path is needed for the links of the buttons
    bar.setPath(currentPage.getHandle());

    //"paragraph" specifies the paragraph evoked by the "Properties" button
    bar.setParagraph("samplesPageProperties");

    // initialize the default buttons (preview, site admin, properties)
    // note: buttons are not placed through init (see below)
    bar.setDefaultButtons();

    // to overwrite single properties of the default buttons, use getButtonXXX() methods:
    bar.getButtonProperties().setLabel("Page properties");


    //add customized buttons to the main bar
    Button fProps=new Button();
    fProps.setLabel("Form properties");
    fProps.setOnclick("mgnlOpenDialog('"+Resource.getActivePage(request).getHandle()+"','','','samplesFormProperties','"+ContentRepository.WEBSITE+"')");
    bar.setButtonsRight(fProps);


    // places the preview and the site admin button to the very left and the properties button to the very right
    bar.placeDefaultButtons();


    //draw the main bar
    bar.drawHtml(out);
]]>
      </jsp:scriptlet>
      <div id="contentDivMainColumn">
        <jsp:scriptlet>
          <![CDATA[
            String alertText = Resource.getActivePage(request).getNodeData("mandatoryAlert").getString();
            if (alertText.equals("")) alertText = "Please fill in all fields marked with an asterisk.";
            alertText = alertText.replaceAll("'", "&rsquo;");
            alertText = alertText.replaceAll("\"", "&rsquo;");
            alertText = alertText.replaceAll("\r\n", "<br/>");
            pageContext.setAttribute("alertText", alertText);
            ]]>
        </jsp:scriptlet>
        <form name="samplesForm" action="${pageContext.request.contextPath}${actpage.handle}.html" method="post"
          onsubmit="return (checkMandatories(this.name,'${alertText}'));">
          <input type="hidden" name="sendMail" value="true" />
          <c:import url="/templates/samples/templates/inc/columnMain.jsp" />
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
              <img src="${pageContext.request.contextPath}/docroot/samples/imgs/poweredSmall.gif" />
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
      <c:import url="/templates/samples/templates/inc/headerImage.jsp" />
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>
