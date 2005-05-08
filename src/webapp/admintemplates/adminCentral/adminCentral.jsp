<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">

    <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />
    <jsp:directive.page import="java.util.Date" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.ButtonSet" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />
    <jsp:directive.page import="info.magnolia.cms.beans.config.Paragraph" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.Icon" />
    <jsp:directive.page import="info.magnolia.cms.core.Content" />
    <jsp:directive.page import="info.magnolia.cms.security.SessionAccessControl" />
    <jsp:directive.page import="info.magnolia.cms.beans.config.Server" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.Sources" />
    <jsp:directive.page import="info.magnolia.module.admininterface.Store" />
    <jsp:directive.page import="java.util.Iterator" />
    <jsp:directive.page import="javax.jcr.RepositoryException" />
    <jsp:directive.page import="info.magnolia.cms.core.ItemType" />
    <jsp:directive.page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" />
	<jsp:directive.page import="info.magnolia.cms.security.Permission"/>

    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <!--<jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>-->
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><fmt:message key="central.title"/></title>
    <cms:links adminOnly="false" />
    </head>

    <body class="mgnlBgDark mgnlAdminMain" onload="mgnlAdminCentralResize();">

    <jsp:scriptlet>
    String labelPost = "";
	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");

	Content userPage=SessionAccessControl.getUserNode(request);
	String userName=userPage.getTitle();
	if (userName.equals("")) userName=userPage.getName();
    pageContext.setAttribute("username", userName);

	//boolean permissionUsers=userPage.getNodeData("permissionUsers").getBoolean();
	boolean permissionUsers = SessionAccessControl.getAccessManager(request,  ContentRepository.USERS).isGranted("/", Permission.WRITE);
	boolean permissionRoles = SessionAccessControl.getAccessManager(request,  ContentRepository.USER_ROLES).isGranted("/", Permission.WRITE);
	boolean permissionConfig = SessionAccessControl.getAccessManager(request,  ContentRepository.CONFIG).isGranted("/", Permission.WRITE);

</jsp:scriptlet>
    <div style="position:absolute;top:3px;right:20px;" class="mgnlText">
    	<fmt:message key="central.loggedInAs">
    		<fmt:param value="${username}"/>
    	</fmt:message>
    </div>

    <jsp:scriptlet>
	if (!Server.isAdmin()) {
</jsp:scriptlet>
    <div style="position:absolute;top:20px;right:20px;" class="mgnlText"><strong>*** <fmt:message>central.publicInstance</fmt:message> ***</strong></div>
    <jsp:scriptlet>
	}
</jsp:scriptlet>

    <div id="mgnlAdminCentral_ButtonsDiv" class="mgnlAdminCentralButtonsDiv"><jsp:scriptlet>

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
		Iterator it=Store.getInstance().getStore().getContent("adminCentral").getContent("buttonList").getChildren(ItemType.CONTENTNODE).iterator();
		while (it.hasNext()) {
			Content c=(Content) it.next();
			Button b=new Button();
			String label = info.magnolia.cms.i18n.TemplateMessagesUtil.get(request, c.getNodeData("label").getString());
			b.setLabel(labelPre + label + labelPost);
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
	b4.setPushButtonTag("div");
	b4.setIconSrc(request.getContextPath() + new Icon().getSrc("about",Icon.SIZE_MEDIUM));
	bs.setButtons(b4);
    pageContext.setAttribute("buttons", bs);

</jsp:scriptlet>
    <div class="mgnlAdminCentralButton">${buttons.html}</div>
    </div>

    <jsp:scriptlet>

    StringBuffer src=new StringBuffer();
	src.append(request.getContextPath());
	src.append("/.magnolia/adminCentral/extractTree.html");
	src.append("?&amp;amp;mgnlCK=");
	src.append(new Date().getTime());
	src.append("&amp;amp;repository="+repository);
	if (path!=null) src.append("&amp;amp;path="+path);
	if (pathOpen!=null) src.append("&amp;amp;pathOpen="+pathOpen);
	if (pathSelected!=null) src.append("&amp;amp;pathSelected="+pathSelected);
	pageContext.setAttribute("framesrc", src.toString());

</jsp:scriptlet>

    <div id="mgnlAdminCentral_ExtractTreeDiv" class="mgnlAdminCentralExtractTreeDiv"><iframe
        id="mgnlAdminCentral_ExtractTreeIFrame" src="${framesrc}" scrolling="no"
        style="border: none; width:100%; height:100%" frameborder="0"><![CDATA[ <!-- a comment here is needed for the correct rendering of the iframe tag -->]]></iframe>
    </div>

    <div id="mgnlAdminCentral_ExtractNonTreeDiv" class="mgnlAdminCentralExtractNonTreeDiv"><iframe
        id="mgnlAdminCentral_ExtractNonTreeIFrame" src="" scrolling="auto" style="border: none; width:100%; height:100%"
        frameborder="0"><![CDATA[ <!-- a comment here is needed for the correct rendering of the iframe tag -->]]></iframe></div>
    </body>
    </html>
</jsp:root>
