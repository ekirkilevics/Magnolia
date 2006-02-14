<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
   <%@ page import = "info.magnolia.module.owfe.OWFEEngine,java.util.Iterator,openwfe.org.engine.workitem.WorkItem,openwfe.org.engine.workitem.InFlowWorkItem, openwfe.org.engine.expressions.FlowExpressionId,openwfe.org.engine.workitem.StringMapAttribute,com.ns.log.Log" %>
   <jsp:useBean id ="owfeBean" scope="application" class="info.magnolia.module.owfe.OWFEBean" />  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Approve</title>
</head>
<body>

<%
String eid = request.getParameter("eid");
if (eid == null)
{
	out.println("error: the eid = null");
}
else 
	out.println("expression id = " + eid);
String[] names = request.getParameterValues("attributeName");
String[] values = request.getParameterValues("value");
for (int i = 0; i < names.length; i++){
	out.println(names[i]+"=");
	out.println(values[i]+"<br>");
}
boolean result = true;
try{
	owfeBean.updateWorkItem(eid, names, values);
}
catch (Exception e)
{
	out.println("<pre>");
	out.println(Log.printExpStack(e));
	e.printStackTrace(); 
	result = false;
	out.println("</pre>");
}
%>
<br>
<%
if (result)
	out.println("<h3>save succeded.</h3>");
else
	out.println("<h3>save failed.</h3>");
%>
<br>
<a href="inbox.jsp"> back to inbox </a>
</body>
</html>
