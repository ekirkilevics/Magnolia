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

%><%@ page import="info.magnolia.cms.beans.config.ContentRepository,
				 java.util.Date,
				 info.magnolia.cms.gui.control.ButtonSet,
				 info.magnolia.cms.gui.control.Button,
				 info.magnolia.cms.gui.dialog.DialogSpacer,
				 info.magnolia.cms.beans.config.Paragraph,
				 info.magnolia.cms.gui.misc.Icon,
				 info.magnolia.cms.core.Content,
				 info.magnolia.cms.security.SessionAccessControl,
				 info.magnolia.cms.beans.config.Server,
				 info.magnolia.cms.gui.dialog.DialogDialog,
				 info.magnolia.cms.gui.misc.Sources"%>
<%
	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");
	String destinationControlName=request.getParameter("mgnlControlName");
	String destinationExtension=request.getParameter("mgnlExtension");
	if (destinationExtension==null) destinationExtension="";

	StringBuffer html=new StringBuffer();
	html.append("<html><head>");
	html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
	html.append(new Sources(request.getContextPath()).getHtmlJs());
	html.append(new Sources(request.getContextPath()).getHtmlCss());
	html.append("</head>");
	html.append("<body class=\"mgnlBgDark\" onload=\"mgnlDialogLinkBrowserResize();\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">");



	StringBuffer src=new StringBuffer();
	src.append(request.getContextPath());
	src.append("/.magnolia/dialogs/linkBrowserIFrame.html");
	src.append("?&amp;mgnlCK="+new Date().getTime());
	src.append("&amp;repository="+repository);
	if (path!=null) src.append("&amp;path="+path);
	if (pathOpen!=null) src.append("&amp;pathOpen="+pathOpen);
	if (pathSelected!=null) src.append("&amp;pathSelected="+pathSelected);


	html.append("<div id=\"mgnlTreeDiv\" class=\"mgnlDialogLinkBrowserTreeDiv\">");
	html.append("<iframe id=\"mgnlDialogLinkBrowserIFrame\" name=\"mgnlDialogLinkBrowserIFrame\" src=\""+src+"\" scrolling=\"no\" frameborder=\"0\" width=\"100%\" height=\"100\"></iframe>");
	html.append("</div>");


	Button bOk=new Button();
	bOk.setLabel("OK"	bOk.setOnclick("mgnlDialogLinkBrowserWriteBack('"+destinationControlName+"','"+destinationExtension+"','" + request.getContextPath() + "');");
);

	Button bCancel=new Button();
	bCancel.setLabel("Cancel");
	bCancel.setOnclick("window.close();");


	html.append("<div class=\""+DialogDialog.CSSCLASS_TABSETSAVEBAR+"\">");
	html.append(bOk.getHtml());
	html.append(" ");
	html.append(bCancel.getHtml());
	html.append("</div>");



	html.append("</body></html>");


	out.println(html);

