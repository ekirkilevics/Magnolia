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

	String data=Resource.getLocalContentNode(request).getNodeData("tableData").getString();

	if (!data.equals("")) {
		boolean header=Resource.getLocalContentNode(request).getNodeData("tableHeader").getBoolean();
		boolean altBg=Resource.getLocalContentNode(request).getNodeData("tableAltBg").getBoolean();
		boolean linesH=Resource.getLocalContentNode(request).getNodeData("tableLinesHorizontal").getBoolean();
		boolean linesV=Resource.getLocalContentNode(request).getNodeData("tableLinesVertical").getBoolean();
		boolean small=Resource.getLocalContentNode(request).getNodeData("tableFontSmall").getBoolean();
		boolean alignRight=Resource.getLocalContentNode(request).getNodeData("tableAlignment").getBoolean();

		out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
		boolean alt=true;

		String[] rows=data.split("\r\n");
		StringBuffer html=new StringBuffer();

		for (int i=0;i<rows.length;i++) {

			html.append("<tr");
			if (altBg && alt) html.append(" class=\"tableAlt\"");
			html.append(">");

			String[] cols=rows[i].split("\t");

			for (int ii=0;ii<cols.length;ii++) {
				String cssClass;
				if (i==0 && header) cssClass="tableHead";
				else cssClass="table";

				html.append("<td class=\""+cssClass+"\" style=\"");
				if (alignRight) {
					if (i!=0) html.append("text-align:right;");
					else  html.append("text-align:center;");
				}
				if (small) html.append("font-size:9px;");
				if (linesV) {
					html.append("border-right-width:1px;");
					if (ii==0) html.append("border-left-width:1px;");
				}
				if (linesH) {
					html.append("border-bottom-width:1px;");
					if (i==0) html.append("border-top-width:1px;");
				}
				html.append("\">");

				if (cols[ii].equals("")) html.append("&nbsp;");
				else html.append(cols[ii]);

				html.append("</td>");
			}
			html.append("</tr>");
			alt=!alt;
		}
		html.append("</table>");
		out.println(html);
	}



%>

