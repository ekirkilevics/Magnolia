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
%><%@ page import="info.magnolia.cms.core.Content,
				   info.magnolia.cms.util.Resource,
				   info.magnolia.cms.gui.misc.Sources,
				   info.magnolia.cms.gui.inline.BarMain,
				   info.magnolia.cms.gui.misc.FileProperties,
				   info.magnolia.cms.gui.control.Button"%>
<%@ taglib uri="cms-taglib" prefix="cms" %>
<%@ taglib uri="cms-util-taglib" prefix="cmsu" %>
<%@ taglib uri="JSTL" prefix="c" %>


<%-- ################################################## --%>
<%-- header image --%>
<%-- ################################################## --%>

<%
    Content activePage=Resource.getActivePage(request);
	String dataName="headerImage";
	String imagePath=null;
	String alt="";

	if (activePage.getNodeData(dataName).isExist()) {
		// if existing, show the image of the page itself
		FileProperties props=new FileProperties(activePage,dataName);
		imagePath=props.getProperty(FileProperties.PATH);
		alt=activePage.getNodeData(dataName+"Alt").getString();
	}
	else {
		// else find the nearest image
		for (int i=activePage.getAncestors().size()-1;i>=0;i--) {
			Content c=activePage.getAncestor(i);
			if (c.getNodeData(dataName).isExist()) {
				FileProperties props=new FileProperties(c,dataName);
				imagePath=props.getProperty(FileProperties.PATH);
				alt=c.getNodeData(dataName+"Alt").getString();
				break;
			}
		}
	}
	if (imagePath==null) {
		//no image found: use default
		imagePath="/docroot/samples/imgs/header.jpg";
		alt="magnolia - for content management";
	}

	out.println("<div style=\"position:absolute;left:0px;top:0px;\">");
	out.println("<img src=\""+imagePath+"\" alt=\""+alt+"\"><br>");
	out.println("</div>");
%>
