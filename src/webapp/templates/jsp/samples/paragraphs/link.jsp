<%@ page import="info.magnolia.cms.util.Resource,
				 info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.core.Content,
				 info.magnolia.cms.security.SessionAccessControl,
				 javax.jcr.RepositoryException"%>
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

	String link=Resource.getLocalContentNode(request).getNodeData("link").getString();
	String text=Resource.getLocalContentNode(request).getNodeData("text").getString();

	if (!link.equals("")) {
		String linkType=Resource.getLocalContentNode(request).getNodeData("linkType").getString();
		StringBuffer html=new StringBuffer();

		html.append("&raquo; <a href=\"");

		if (linkType.equals("external")) {
			//if no protocol is defined, add http:// to link
			if (html.indexOf("://")==-1) html.append("http://"+link+"\" target=\"_blank\">");
			if (!text.equals("")) html.append(text);
			else html.append(link);
		}
		else {
			html.append(link+".html\">");
			if (!text.equals("")) html.append(text);
			else {
				try {
					// get title of linked page
					HierarchyManager hm=SessionAccessControl.getHierarchyManager(request);
					Content destinationPage=hm.getContent(link);
					html.append(destinationPage.getNodeData("title").getString());
				}
				catch (RepositoryException re) {
					html.append(link);
				}
			}
		}
		html.append("</a>");

		out.println(html);
	}


%>

