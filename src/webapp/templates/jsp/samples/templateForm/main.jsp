<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">

<jsp:directive.page import="info.magnolia.cms.core.Content" />
<jsp:directive.page import="org.apache.log4j.Logger" />
<jsp:directive.page import="info.magnolia.cms.util.Resource" />
<jsp:directive.page import="info.magnolia.cms.beans.runtime.MultipartForm" />
<jsp:directive.page import="java.util.Enumeration" />
<jsp:directive.page import="java.util.Collection" />
<jsp:directive.page import="java.util.Iterator" />
<jsp:directive.page import="info.magnolia.cms.core.ContentNode" />
<jsp:directive.page import="javax.mail.internet.InternetAddress" />
<jsp:directive.page import="javax.mail.internet.AddressException" />
<jsp:directive.page import="javax.mail.Message" />
<jsp:directive.page import="javax.mail.Session" />
<jsp:directive.page import="java.util.Properties" />
<jsp:directive.page import="javax.mail.internet.MimeMessage" />
<jsp:directive.page import="javax.mail.Transport" />
<jsp:directive.page import="info.magnolia.cms.core.Content" />
<jsp:directive.page import="info.magnolia.cms.beans.config.Server" />
<jsp:directive.page import="java.net.URLEncoder" />
<jsp:directive.page import="java.io.UnsupportedEncodingException" />


<jsp:declaration>
<![CDATA[
	public class MailHandler {

		private String body;
		private String subject;
		private String from;
		private InternetAddress[] toList;
		private InternetAddress[] ccList;
		private Session session;
		//private static final String mailHost = "mail.obinary.com";

		public MailHandler(String mailHost,int toListLength,int ccListLength) throws Exception {
			toList = new InternetAddress[toListLength];
			ccList = new InternetAddress[ccListLength];
			Properties props = System.getProperties();
			props.put("mail.smtp.host", mailHost);
			session = Session.getInstance(props,null);
		}


		public void sendMail() throws Exception {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.getFrom()));
			message.setRecipients(Message.RecipientType.TO, this.getToList());
			message.setRecipients(Message.RecipientType.CC,this.getCcList());
			message.setSubject(subject);

			message.setContent(body, "text/plain");
			message.setHeader("Content-Type","text/plain; charset=UTF-8");

			Transport.send(message);
			//System.out.println("Mail sent!");
		}

		public void setBody(String body) {
			this.body = body;
		}

		public String getBody() {
			return this.body;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getSubject() {
			return this.subject;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getFrom() {
			return this.from;
		}

		public void setToList(String to) throws AddressException{
			String[] toObj=to.split("\n");
			for (int i=0;i<toObj.length;i++) {
				//System.out.println(i+"::"+toObj[i]);
				this.toList[i] = new InternetAddress(toObj[i]);
			}
		}

		public InternetAddress[] getToList() {
			return this.toList;
		}

		public void setCcList(String to) throws AddressException{
			String[] toObj=to.split("\n");
			for (int i=0;i<toObj.length;i++) {
				//System.out.println(i+"::"+toObj[i]);
				this.ccList[i] = new InternetAddress(toObj[i]);
			}
		}

		public InternetAddress[] getCcList() {
			return this.ccList;
		}


	}
]]>
</jsp:declaration>
<jsp:scriptlet>
<![CDATA[


    if (request.getParameter("sendMail")!=null) {
		StringBuffer body=new StringBuffer();
		//build and send email
		Iterator it=Resource.getActivePage(request).getContentNode("mainColumnParagraphs").getChildren().iterator();
		while (it.hasNext()) {
			ContentNode node=(ContentNode) it.next();
			if (request.getParameterValues(node.getName())!=null) {
				String[] values=request.getParameterValues(node.getName());
				body.append(node.getNodeData("title").getString()+"\n");
				for (int i=0;i<values.length;i++) {
					body.append(values[i]+"\n");
				}
				body.append("\n\n");
			}
		}


		String subject=Resource.getActivePage(request).getNodeData("subject").getString();
		String from=Resource.getActivePage(request).getNodeData("from").getString();
		String to=Resource.getActivePage(request).getNodeData("to").getString();
		String cc=Resource.getActivePage(request).getNodeData("cc").getString();
		String server=Resource.getActivePage(request).getNodeData("server").getString();
		if (server.equals("")) server=Server.getDefaultMailServer();
		String redirect=Resource.getActivePage(request).getNodeData("redirect").getString();

		if (!to.equals("") && !from.equals("") && !server.equals("")) {

			String[] toObj=to.split("\n");

			String[] ccObj;
			int ccLength=0;
			if (!cc.equals("")) {
				ccObj=cc.split("\n");
				ccLength=ccObj.length;
			}

			MailHandler mail=new MailHandler(server,toObj.length,ccLength);
			mail.setFrom(from);
			mail.setToList(to);
			if (ccLength!=0) mail.setCcList(cc);
			mail.setSubject(subject);
			mail.setBody(body.toString());
			try {
				mail.sendMail();
			}
			catch (Exception e) {}

			response.sendRedirect(redirect);
		}
	}
]]>
</jsp:scriptlet>

<jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
</jsp:text>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <c:import url="/templates/jsp/samples/global/head.jsp" />
    </head>
	<body>
	<c:import url="/templates/jsp/samples/templateForm/mainBar.jsp"/>

	<div id="contentDivMainColumn">
	<jsp:scriptlet>
	<![CDATA[
		String alertText=Resource.getActivePage(request).getNodeData("mandatoryAlert").getString();
		if (alertText.equals("")) alertText="Please fill in all fields marked with an asterisk.";
		alertText=alertText.replaceAll("'","&rsquo;");
		alertText=alertText.replaceAll("\"","&rsquo;");
		alertText=alertText.replaceAll("\r\n","<br/>");
		pageContext.setAttribute("alertText", alertText);
		]]>
	</jsp:scriptlet>
		<form name="samplesForm" action="${actpage.handle}.html" method="post" onsubmit="return (checkMandatories(this.name,'${alertText}'));">
			<input type="hidden" name="sendMail" value="true"/>
			<c:import url="/templates/jsp/samples/global/columnMain.jsp"/>
			<c:import url="/templates/jsp/samples/templateForm/columnMainNewBar.jsp"/>
			<c:import url="/templates/jsp/samples/global/footer.jsp"/>
		</form>
	</div>

	<div id="contentDivRightColumn">
		<c:import url="/templates/jsp/samples/global/columnRight.jsp"/>
	</div>

	<c:import url="/templates/jsp/samples/global/headerImage.jsp"/>
	            <cmsu:simpleNavigation />

	</body>
</html>
</jsp:root>
