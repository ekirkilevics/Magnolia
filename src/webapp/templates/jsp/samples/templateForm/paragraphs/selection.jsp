<%@ page import="info.magnolia.cms.util.Resource"%>
<%
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



	String name=Resource.getLocalContentNode(request).getName();
	String title=Resource.getLocalContentNode(request).getNodeData("title").getString();
	if (!title.equals("")) {
		out.print("<h2>"+title);
		if (Resource.getLocalContentNode(request).getNodeData("mandatory").getBoolean()) {
			out.println("*");
			out.println("<input type=\"hidden\" name=\"mgnlMandatory\" value=\""+name+"\">");
		}
		out.println("</h2>");
	}


	String type=Resource.getLocalContentNode(request).getNodeData("type").getString();
	String[] values=Resource.getLocalContentNode(request).getNodeData("values").getString().split("\r\n");

	if (type.equals("select")) {
		out.println("<select name=\""+name+"\">");
		for (int i=0;i<values.length;i++) {
			out.println("<option value=\""+values[i]+"\">"+values[i]+"</option>");
		}
		out.println("</select>");
	}
	else {
		for (int i=0;i<values.length;i++) {
			out.println("<input name=\""+name+"\" type=\""+type+"\" class=\""+type+"\"value=\""+values[i]+"\">"+values[i]+"<br/>");
		}
	}

%>