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
%><%@ page import="info.magnolia.cms.license.License,
				 info.magnolia.cms.gui.misc.Sources,
				 info.magnolia.cms.beans.config.Server"%>
<html>
<head>
	<title>About Magnolia</title>

 	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<%
	out.println(new Sources().getHtmlJs());
	out.println(new Sources().getHtmlCss());
	%>

</head>

<body class="mgnlBgLight">

		<div class="mgnlText" style="position:absolute;top:15px;left:20px;">
		Magnolia 2.0<br>
		<br>
		<%
        License license = License.getInstance();
		out.println("Version: "+license.get(License.VERSION_NUMBER)+"<br>");
		out.println("Build: "+license.get(License.BUILD_NUMBER)+"<br>");
		out.println("<br>");
		//out.println(License.getProviderName()+"<br>");

		String[] address=license.get(License.PROVIDER_ADDRESS).split("\n");
		for (int i=0; i<address.length; i++) {
			out.println(address[i]+"<br>");
		}
		out.println("<br>");

		String domain=license.get(License.PRODUCT_DOMAIN);
		out.println("<a href=http://"+domain+license.get(License.VERSION_PAGE_HANDLE)+" target=_blank style=color:black;>"+domain+"</a><br>");

		String email=license.get(License.PRIVIDER_EMAIL);
		out.println("<a href=mailto:"+email+" style=color:black;>"+email+"</a><br>");

		%>
		<br><br>
		</div>

		<div class="mgnlText" style="position:absolute;top:15px;left:220px;width:520px;	border-style:solid;border-width:1px;border-top-color:#999999;border-left-color:#999999;border-bottom-color:#CCCCCC;border-right-color:#CCCCCC;">
			<iframe src="http://www.magnolia.info/en/admincentral/aboutmagnolia.html?magnoliaVersion=<%=license.get(License.VERSION_NUMBER)%>&magnoliaBuild=<%=license.get(License.BUILD_NUMBER)%>&serverOS=<%=license.getOSName()%>&isAdmin=<%=Server.isAdmin()%>%>" frameborder="0" width="100%" height="400"></iframe>
		</div>


</body>
</html>