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
				 info.magnolia.cms.gui.misc.Sources,
				 info.magnolia.module.admininterface.Store,
				 java.util.Iterator,
				 javax.jcr.RepositoryException,
				 info.magnolia.cms.core.ContentNode,
				 info.magnolia.cms.beans.config.ItemType"
%>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>
<%--
page layout only works in quirk mode at the moment
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
--%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Magnolia: AdminCentral</title>
  <%=new Sources(request.getContextPath()).getHtmlJs()%>
  <%=new Sources(request.getContextPath()).getHtmlCss()%>
</head>
<body class="mgnlBgDark" onload="mgnlAdminCentralResize();">
<div style="position:absolute;top:32px;left:17px;">
<img src="<%=request.getContextPath()%>/admindocroot/adminCentral/magnoliaLogo.gif" />
</div>
<%
    String labelPost = "";
	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");

	Content userPage=SessionAccessControl.getUserNode(request);
	String userName=userPage.getTitle();
	if (userName.equals("")) userName=userPage.getName();
	//todo: node data name not static! see usersEdit/main.jsp
	boolean permissionUsers=userPage.getNodeData("permissionUsers").getBoolean();
	boolean permissionRoles=userPage.getNodeData("permissionRoles").getBoolean();
	boolean permissionConfig=userPage.getNodeData("permissionConfig").getBoolean();

%>
	<div style="position:absolute;top:3px;right:20px;" class="mgnlText">
	You are logged in as <%=userName%>
	</div>
<%
	if (!Server.isAdmin()) {
    %>
		<div style="position:absolute;top:20px;right:20px;" class="mgnlText">
		<strong>*** Public instance ***</strong>
		</div>
	<%
	}
%>

<div style="position:absolute;top:102px;left:8px;">
</div>
<div id="mgnlAdminCentral_ButtonsDiv" class="mgnlAdminCentralButtonsDiv">
<%

	String labelPre="";

	ButtonSet bs=new ButtonSet();
	bs.setButtonType(ButtonSet.BUTTONTYPE_PUSHBUTTON);
	bs.setCssClass("mgnlAdminCentralButton");

	Button b0=new Button();
	b0.setLabel(labelPre+LocaleSupport.getLocalizedMessage(pageContext, "menu.website")+labelPost);
	b0.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.WEBSITE+"');");
	b0.setPushButtonTag("div");
	b0.setIconSrc(request.getContextPath() + new Icon().getSrc("earth",Icon.SIZE_MEDIUM));
	if (repository.equals(ContentRepository.WEBSITE)) b0.setState(Button.BUTTONSTATE_PUSHED);
	bs.setButtons(b0);

	if (permissionUsers) {
		Button b1=new Button();
		b1.setLabel(labelPre+LocaleSupport.getLocalizedMessage(pageContext, "menu.users")+labelPost);
		b1.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.USERS+"');");
		b1.setPushButtonTag("div");
		b1.setIconSrc(request.getContextPath() + new Icon().getSrc(Icon.USER,Icon.SIZE_MEDIUM));
		if (repository.equals(ContentRepository.USERS)) b1.setState(Button.BUTTONSTATE_PUSHED);
		bs.setButtons(b1);
	}

	if (permissionRoles) {
		Button b2=new Button();
		b2.setLabel(labelPre+LocaleSupport.getLocalizedMessage(pageContext, "menu.roles")+labelPost);
		b2.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.USER_ROLES+"');");
		b2.setPushButtonTag("div");
		b2.setIconSrc(request.getContextPath() + new Icon().getSrc(Icon.ROLE,Icon.SIZE_MEDIUM));
		if (repository.equals(ContentRepository.USER_ROLES)) b2.setState(Button.BUTTONSTATE_PUSHED);
		bs.setButtons(b2);
	}

	if (permissionConfig) {
		Button b3=new Button();
		b3.setLabel(labelPre+LocaleSupport.getLocalizedMessage(pageContext, "menu.config")+labelPost);
		b3.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.CONFIG+"');");
		b3.setPushButtonTag("div");
		b3.setIconSrc(request.getContextPath() + new Icon().getSrc("gears",Icon.SIZE_MEDIUM));
		if (repository.equals(ContentRepository.CONFIG)) b3.setState(Button.BUTTONSTATE_PUSHED);
		bs.setButtons(b3);
	}

	//customized buttons
	try {
		Iterator it=Store.getInstance().getStore().getContent("adminCentral").getContent("buttonList").getChildren(ItemType.NT_CONTENTNODE).iterator();
		while (it.hasNext()) {
			ContentNode c=(ContentNode) it.next();
			Button b=new Button();
			b.setLabel(labelPre + c.getNodeData("label").getString() + labelPost);
			b.setPushButtonTag("div");
			b.setOnclick(c.getNodeData("onclick").getString());
			if (c.getNodeData("iconSrc").getString().equals("")) b.setIconSrc(request.getContextPath() + new Icon().getSrc("pens",Icon.SIZE_MEDIUM));
			else b.setIconSrc(request.getContextPath() + c.getNodeData("iconSrc").getString());
			bs.setButtons(b);
		}
	}
	catch (Exception re) {}


	Button b4=new Button();
	b4.setCssClass(bs.getCssClass());
	b4.setLabel(labelPre+LocaleSupport.getLocalizedMessage(pageContext, "menu.about")+labelPost);
	b4.setOnclick("mgnlAdminCentralSwitchExtractNonTree('/.magnolia/adminCentral/extractAbout.html');");
	b4.setLabelNbspPadding(0);
	b4.setPushButtonTag("div");
	b4.setIconSrc(request.getContextPath() + new Icon().getSrc("about",Icon.SIZE_MEDIUM));
	bs.setButtons(b4);

%>
<%=bs.getHtml()%>
<div class="mgnlAdminCentralButton" style="height:1px"></div>
</div>
<%
	StringBuffer src=new StringBuffer();
	src.append(request.getContextPath());
	src.append("/.magnolia/adminCentral/extractTree.html");
	src.append("?&amp;mgnlCK="+new Date().getTime());
	src.append("&amp;repository="+repository);
	if (path!=null) src.append("&amp;path="+path);
	if (pathOpen!=null) src.append("&amp;pathOpen="+pathOpen);
	if (pathSelected!=null) src.append("&amp;pathSelected="+pathSelected);
%>

<div id="mgnlAdminCentral_ExtractTreeDiv" class="mgnlAdminCentralExtractTreeDiv">
<iframe id="mgnlAdminCentral_ExtractTreeIFrame" src="<%=src%>" scrolling="no" frameborder="0" width="100%" height="100%"></iframe>
</div>

<div id="mgnlAdminCentral_ExtractNonTreeDiv" class="mgnlAdminCentralExtractNonTreeDiv">
<iframe id="mgnlAdminCentral_ExtractNonTreeIFrame" scrolling="auto" frameborder="0" width="100%" height="100%"></iframe>
</div>
</body></html>