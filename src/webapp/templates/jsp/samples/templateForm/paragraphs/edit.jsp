<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.util.Resource" />
<jsp:scriptlet>
<![CDATA[


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
		out.println("<input type=\"text\" name=\""+name+"\" class=\"text\"><br/>");
	}
	else {
		out.println("<textarea name=\""+name+"\" rows=\""+rows+"\" cols=\"300\"></textarea>");
	}
]]>
</jsp:scriptlet>
</jsp:root>






