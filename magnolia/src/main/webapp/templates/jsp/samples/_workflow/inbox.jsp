<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import = "info.magnolia.module.owfe.OWFEEngine,java.util.Iterator,openwfe.org.engine.workitem.WorkItem,openwfe.org.engine.workitem.InFlowWorkItem, openwfe.org.engine.expressions.FlowExpressionId,openwfe.org.engine.workitem.StringMapAttribute, com.ns.log.Log" %>
<jsp:useBean id ="owfeBean" scope="application" class="info.magnolia.module.owfe.OWFEBean" />  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
 <link rel="stylesheet" href="style.css" type="text/css" />
<title>Inbox</title>
</head>
<body>

<%
int size = owfeBean.getWorkItemsNumber(request);
%>
Inbox for project lead (<%=size%> work items)
<table border=1 >

<tr align="center"><b><td>index</td><td>FlowExpressionId</td><td>Dispatch time</td><td>Attributes</b></td><td>action</td></tr>
<%


for (int i = 0; i < size; i++){ 
InFlowWorkItem wi = (InFlowWorkItem)owfeBean.getWorkItem(request, i);
if (wi != null){
String id = wi.getLastExpressionId().toParseableString();
%>
<tr><td>
<%=i %></td>

<td width="30">
<font size=2>
<%=id %>
</font>
<td width="30">
<font size=2>
<%
out.println(wi.getDispatchTime());
%>
</font>
</td>
<td>
<form action="save.jsp" method="get">
<input type="hidden" name="eid" value="<%=id%>">
<%

// list attribute
StringMapAttribute map = wi.getAttributes();
Iterator it = map.alphaStringIterator();

while (it.hasNext())
{
	String name = (String)it.next();
	Log.trace("web", "attribute name = " + name);
	if (name.equals("__definition__"))
		continue;
	String value = (String)wi.getAttribute(name).toString();
	Log.trace("web", "attribute value = " + value);	
	%>
	 
	<%=name%><input type="hidden" name="attributeName" value="<%=name%>"/>
	
	:<input name="value" value="<%=value%>"/>
	<!-- 
<textarea rows=1 name="<%=value%>" ><%=value%></textarea> -->
	<br>

<%} %>
<input type="submit" value="save"/>
</form>
</td>
 
<td><a href="approve.jsp?eid=<%=id %>">approve</a>  <a href="reject.jsp?eid=<%=id %>">reject</a></tr>
<%}} %>
</br>
</table>




</body>
</html>
