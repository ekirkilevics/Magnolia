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
                   info.magnolia.cms.gui.dialog.DialogSuper,
                   info.magnolia.cms.gui.control.Edit"%>


<%
	DialogSuper control=(DialogSuper) pageContext.getAttribute("dialogObject",PageContext.REQUEST_SCOPE);

	//access all the values of the dialog definition with getConfigValue
	String colorStart=control.getConfigValue("colorStart");
	String colorOffset=control.getConfigValue("colorOffset");

	int start=Integer.parseInt(colorStart,16);
	int offset=Integer.parseInt(colorOffset,16);


	out.println("<table cellpadding=\"0\" cellspacing=\"2\" border=\"0\" height=\"60\"><tr>");
	for (int i=start;i>0;i-=offset) {
		//
		String color=Integer.toHexString(i);
		while(color.length()<6) {
			color="0"+color;
		}

		out.println("<td" +
		 		" style=\"background-color:#"+color+";width:60px;\");" +
				" onclick=\"document.getElementById('"+control.getName()+"').value='"+color+"'\">" +
				"&nbsp;</td>");
	}
	out.println("</tr></table><br>");



	//for form elements use the magnolia control package
	//toggle alt to get two different outputs
	out.println("Selected value:<br>");
	Edit editControl=new Edit(control.getName(),control.getWebsiteNode());
	editControl.setCssClass(DialogSuper.CSSCLASS_EDIT);
	editControl.setCssStyles("width","60");
	out.println(editControl.getHtml());



%>
