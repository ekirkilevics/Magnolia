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
				 info.magnolia.module.adminInterface.Store,
				 java.util.Iterator,
				 javax.jcr.RepositoryException,
				 info.magnolia.cms.core.ContentNode,
				 info.magnolia.cms.beans.config.ItemType"%>
<%
	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");

	StringBuffer html=new StringBuffer();
	html.append("<html><head>");
	html.append("<title>Magnolia: AdminCentral</title>");
	html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
	html.append(new Sources().getHtmlJs());
	html.append(new Sources().getHtmlCss());
	html.append("</head>");

	html.append("<body class=\"mgnlBgDark\" onload=\"mgnlAdminCentralResize();\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">");

	html.append("<div style=\"position:absolute;top:32px;left:17px;\">");
	html.append("<img src=/admindocroot/adminCentral/magnoliaLogo.gif>");
	html.append("</div>");


	Content userPage=SessionAccessControl.getUserNode(request);
	String userName=userPage.getTitle();
	if (userName.equals("")) userName=userPage.getName();
	//todo: node data name not static! see usersEdit/main.jsp
	boolean permissionUsers=userPage.getNodeData("permissionUsers").getBoolean();
	boolean permissionRoles=userPage.getNodeData("permissionRoles").getBoolean();
	boolean permissionConfig=userPage.getNodeData("permissionConfig").getBoolean();


	html.append("<div style=\"position:absolute;top:3px;right:20px;\" class=\"mgnlText\">");
	html.append("You are logged in as "+userName);
	html.append("</div>");

	if (!Server.isAdmin()) {
		html.append("<div style=\"position:absolute;top:20px;right:20px;\" class=\"mgnlText\">");
		html.append("<b>*** Public instance ***</b><br>");
		html.append("</div>");
	}



	//html.append("<a onclick=\"document.getElementById(mgnlWebsiteTree.name+'_DivSuper').style.display='block';document.getElementById(mgnlConfigTree.name+'_DivSuper').style.display='none';mgnlWebsiteTree.resize();\">website</a>");

	html.append("<div style=\"position:absolute;top:102px;left:8px;\">");

	html.append("</div>");


	html.append("<div id=\"mgnlAdminCentral_ButtonsDiv\" class=\"mgnlAdminCentralButtonsDiv\">");

	ButtonSet bs=new ButtonSet();

	bs.setButtonType(ButtonSet.BUTTONTYPE_PUSHBUTTON);
	//bs.setHtmlInter(new DialogSpacer().getHtml(1));
	bs.setCssClass("mgnlAdminCentralButton");

	String labelPre="<span style=\"position:relative;top:-6px;left:5px;\">";
	String labelPost="</span>";

	Button b0=new Button();
	b0.setLabel(labelPre+"Website"+labelPost);
	//b0.setOnclick("mgnlAdminCentralSwitchTree('mgnlWebsiteTree','"+ContentRepository.WEBSITE+"');");
	b0.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.WEBSITE+"');");
	//b0.setOnclick("mgnlAdminCentralSwitchTreeX('"+iFrameSrc+"','"+ContentRepository.WEBSITE+"');");
	b0.setLabelNbspPadding(0);
	b0.setPushButtonTag("div");
	b0.setIconSrc(new Icon().getSrc("earth",Icon.SIZE_MEDIUM));
	if (repository.equals(ContentRepository.WEBSITE)) b0.setState(Button.BUTTONSTATE_PUSHED);
	bs.setButtons(b0);



	if (permissionUsers) {
		Button b1=new Button();
		b1.setLabel(labelPre+"Users"+labelPost);
		//b1.setOnclick("mgnlAdminCentralSwitchTree('mgnlUsersTree','"+ContentRepository.USERS+"');");
		//b1.setOnclick("mgnlAdminCentralSwitchTreeX('"+iFrameSrc+"','"+ContentRepository.USERS+"');");
		b1.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.USERS+"');");
		b1.setLabelNbspPadding(0);
		b1.setPushButtonTag("div");
		b1.setIconSrc(new Icon().getSrc(Icon.USER,Icon.SIZE_MEDIUM));
		if (repository.equals(ContentRepository.USERS)) b1.setState(Button.BUTTONSTATE_PUSHED);
		bs.setButtons(b1);
	}

	if (permissionRoles) {
		Button b2=new Button();
		b2.setLabel(labelPre+"Roles"+labelPost);
		//b2.setOnclick("mgnlAdminCentralSwitchTree('mgnlUserRolesTree');");
		b2.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.USER_ROLES+"');");
		b2.setLabelNbspPadding(0);
		b2.setPushButtonTag("div");
		b2.setIconSrc(new Icon().getSrc(Icon.ROLE,Icon.SIZE_MEDIUM));
		if (repository.equals(ContentRepository.USER_ROLES)) b2.setState(Button.BUTTONSTATE_PUSHED);
		bs.setButtons(b2);
	}

	if (permissionConfig) {
		Button b3=new Button();
		b3.setLabel(labelPre+"Config"+labelPost);
		//b3.setOnclick("mgnlAdminCentralSwitchTree('mgnlConfigTree');");
		b3.setOnclick("mgnlAdminCentralSwitchExtractTree('"+ContentRepository.CONFIG+"');");
		b3.setLabelNbspPadding(0);
		b3.setPushButtonTag("div");
		b3.setIconSrc(new Icon().getSrc("gears",Icon.SIZE_MEDIUM));
		if (repository.equals(ContentRepository.CONFIG)) b3.setState(Button.BUTTONSTATE_PUSHED);
		bs.setButtons(b3);
	}

	//customized buttons
	try {
		Iterator it=Store.getInstance().getStore().getContent("adminCentral").getContent("buttonList").getChildren(ItemType.NT_CONTENTNODE).iterator();
		while (it.hasNext()) {
			ContentNode c=(ContentNode) it.next();
			Button b=new Button();
			b.setLabel(labelPre+c.getNodeData("label").getString()+labelPost);
			b.setLabelNbspPadding(0);
			b.setPushButtonTag("div");
			b.setOnclick(c.getNodeData("onclick").getString());
			if (c.getNodeData("iconSrc").getString().equals("")) b.setIconSrc(new Icon().getSrc("pens",Icon.SIZE_MEDIUM));
			else b.setIconSrc(c.getNodeData("iconSrc").getString());
			bs.setButtons(b);
		}
	}
	catch (Exception re) {}


	Button b4=new Button();
	b4.setCssClass(bs.getCssClass());
	b4.setLabel(labelPre+"About Magnolia"+labelPost);
	b4.setOnclick("mgnlAdminCentralSwitchExtractNonTree('/.magnolia/adminCentral/extractAbout.html');");
	b4.setLabelNbspPadding(0);
	b4.setPushButtonTag("div");
	b4.setIconSrc(new Icon().getSrc("about",Icon.SIZE_MEDIUM));
	bs.setButtons(b4);

	html.append(bs.getHtml());


	html.append("<div class=\""+bs.getCssClass()+"\" style=\"height:1px\"></div>");

	html.append("</div>");




	StringBuffer src=new StringBuffer("/.magnolia/adminCentral/extractTree.html");
	src.append("?&mgnlCK="+new Date().getTime());
	src.append("&repository="+repository);
	if (path!=null) src.append("&path="+path);
	if (pathOpen!=null) src.append("&pathOpen="+pathOpen);
	if (pathSelected!=null) src.append("&pathSelected="+pathSelected);


	html.append("<div id=\"mgnlAdminCentral_ExtractTreeDiv\" class=\"mgnlAdminCentralExtractTreeDiv\">");
	html.append("<iframe id=\"mgnlAdminCentral_ExtractTreeIFrame\" src=\""+src+"\" scrolling=\"no\" frameborder=\"0\" width=\"100%\" height=\"100\"></iframe>");
	html.append("</div>");

	html.append("<div id=\"mgnlAdminCentral_ExtractNonTreeDiv\" class=\"mgnlAdminCentralExtractNonTreeDiv\">");
	html.append("<iframe id=\"mgnlAdminCentral_ExtractNonTreeIFrame\" src=\"\" scrolling=\"auto\" frameborder=\"0\" width=\"100%\" height=\"100\"></iframe>");
	html.append("</div>");



	html.append("</body></html>");


	out.println(html);


%>