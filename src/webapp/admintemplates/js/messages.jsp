<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page">
	<jsp:directive.page language="java"/>
	<jsp:directive.page import="info.magnolia.cms.i18n.ContextMessages"/>
	<jsp:expression>
		ContextMessages.getInstanceSafely(request).generateJavaScript() 
	</jsp:expression>
</jsp:root>
