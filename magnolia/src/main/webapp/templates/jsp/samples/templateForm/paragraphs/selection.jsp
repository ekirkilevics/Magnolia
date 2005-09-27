<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.util.Resource" />
<jsp:scriptlet>
<![CDATA[



	String name=Resource.getLocalContentNode(request).getName();
	String title=Resource.getLocalContentNode(request).getNodeData("title").getString();
	if (!title.equals("")) {
		out.print("<h2>"+title);
		if (Resource.getLocalContentNode(request).getNodeData("mandatory").getBoolean()) {
			out.println("*");
			out.println("<input type=\"hidden\" name=\"mgnlMandatory\" value=\""+name+"\" />");
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
			out.println("<input name=\""+name+"\" type=\""+type+"\" class=\""+type+"\" value=\""+values[i]+"\" />"+values[i]+"<br/>");
		}
	}

]]>
</jsp:scriptlet>
</jsp:root>
