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


	String title=Resource.getLocalContentNode(request).getNodeData("title").getString();
	String name=Resource.getLocalContentNode(request).getName();
	if (!title.equals("")) {
		out.print("<h2>"+title);
		if (Resource.getLocalContentNode(request).getNodeData("mandatory").getBoolean()) {
			out.println("*");
			out.println("<input type=\"hidden\" name=\"mgnlMandatory\" value=\""+name+"\">");
		}
		out.println("</h2>");
	}


	long rows=Resource.getLocalContentNode(request).getNodeData("rows").getLong();

	if (rows==1) {
		out.println("<input type=\"text\" name=\""+name+"\" class=\"text\"><br>");
	}
	else {
		out.println("<textarea name=\""+name+"\" rows=\""+rows+"\" cols=\"300\"></textarea>");
	}
%>






