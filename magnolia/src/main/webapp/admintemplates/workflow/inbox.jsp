
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="openwfe.org.engine.workitem.InFlowWorkItem" %>
<%@ page import="openwfe.org.engine.workitem.StringMapAttribute" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>


<jsp:useBean id ="owfeBean" scope="application" class="info.magnolia.module.owfe.OWFEBean" />  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/admindocroot/css/workflow.css" />
<title>Inbox</title>
</head>
<body>

<%
List list = owfeBean.getWorkItems(request);
if (list != null){
int size = list.size();
%>
<table class="nicetable">

<tr>
<th class="nicetableheader">index</th>
<th class="nicetableheader">FlowExpressionId</th>
<th class="nicetableheader">Last Modify</th>
<th class="nicetableheader">Attributes</th>
<th class="nicetableheader">action</th>
</tr>
<%
for (int i = 0; i < size; i++){ 
InFlowWorkItem wi = (InFlowWorkItem)list.get(i);
if (wi != null){
String id = wi.getId().toParseableString();
%>
<tr class="nicetablerow"><td>
<%=i %></td>

<td width="30">
<font size=2>
<input width=20 value="
<%=id %>"/>
</font>
<td width="30">
<font size=2>
<%
out.println(wi.getLastModified());
%></font>
</td>
<td>
<form action="${pageContext.request.contextPath}/.magnolia/save.html" method="get">
<input type="hidden" name="eid" value="<%=id%>">
<%

// list attribute
StringMapAttribute map = wi.getAttributes();
Iterator it = map.alphaStringIterator();

while (it.hasNext())
{
    String name = (String)it.next();
    if (name.equals("__definition__"))
        continue;
    String value = (String)wi.getAttribute(name).toString();    
    %>
     
    <%=name%><input type="hidden" name="attributeName" value="<%=name%>"/>
    
    :<input name="value" value="<%=value%>"/>
    <!-- 
<textarea rows=1 name="<%=value%>" ><%=value%></textarea> -->
    <br>

<%
	} // while

%>
<input type="submit" value="save"/>
</form>
</td>
 
<td><a href="${pageContext.request.contextPath}/.magnolia/approve.html?eid=<%=id %>">approve</a>  <a href="${pageContext.request.contextPath}/.magnolia/reject.html?eid=<%=id %>">reject</a>

<br/>
Assign work item to:
<form action="${pageContext.request.contextPath}/.magnolia/assign.html" method="get">
<input name="assignee">
<input type="hidden" name="eid" value="<%=id%>">
<input type="submit" value="assign"/>
</form>

</td></tr>

<%} // if (wi != null)
} // for

} //if (list != null)


 %>
</table>




</body>
</html>
