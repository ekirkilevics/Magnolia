<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:useBean id ="owfeBean" scope="application" class="info.magnolia.module.owfe.OWFEBean" />  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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

boolean result = true;
try{
    owfeBean.rejectActivation(eid);
}
catch (Exception e)
{
    out.println("<pre>");
    e.printStackTrace(); 
    result = false;
    out.println("</pre>");
}
%>
<br>
<%
if (result)
    out.println("<h3>Reject succeded.</h3>");
else
    out.println("<h3>Reject failed.</h3>");
%>
<br>
<a href="${pageContext.request.contextPath}/.magnolia/inbox.html"> back to inbox </a>
</body>
</html>
