<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page">
	<jsp:directive.page language="java"/>
	<jsp:directive.page import="info.magnolia.cms.i18n.MessagesManager"/>
	<jsp:expression>
		MessagesManager.getMessages(request).generateJavaScript() 
	</jsp:expression>
</jsp:root>
