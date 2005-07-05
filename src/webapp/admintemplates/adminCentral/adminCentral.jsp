<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:cms="urn:jsptld:cms-taglib" xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">

    <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />
    <jsp:directive.page import="java.util.Date" />
    <jsp:directive.page import="java.util.Collection" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.ButtonSet" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.ControlSuper" />
    <jsp:directive.page import="info.magnolia.cms.core.Content" />
    <jsp:directive.page import="info.magnolia.cms.core.ContentHandler" />
    <jsp:directive.page import="info.magnolia.cms.security.SessionAccessControl" />
    <jsp:directive.page import="info.magnolia.cms.beans.config.Server" />
    <jsp:directive.page import="info.magnolia.cms.i18n.MessagesManager" />
    <jsp:directive.page import="java.util.Iterator" />
    <jsp:directive.page import="info.magnolia.cms.core.ItemType" />

    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <!--<jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>-->
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><fmt:message key="central.title" /></title>
    <cms:links adminOnly="false" />
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/admindocroot/favicon.ico" type="image/x-icon" />
    </head>

    <body class="mgnlBgDark mgnlAdminMain" onload="mgnlAdminCentralResize();">

    <jsp:scriptlet>

	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");

	String firstOnClick="";
	String iFrameSrc="";
	boolean first = true;

	// this is a workaround for the AdminCentral button in the page edit dialog
    if(pathSelected!= null &amp;&amp; pathSelected!=""){
	    StringBuffer src=new StringBuffer();
		src.append(request.getContextPath());
		src.append("/.magnolia/adminCentral/extractTree.html");
		src.append("?mgnlCK=");
		src.append(new Date().getTime());
		src.append("&amp;repository="+repository);
		if (path!=null) src.append("&amp;path="+path);
		if (pathOpen!=null) src.append("&amp;pathOpen="+pathOpen);
		if (pathSelected!=null) src.append("&amp;pathSelected="+pathSelected);
		iFrameSrc = src.toString();
	}


	ButtonSet bs=new ButtonSet();
	bs.setButtonType(ControlSuper.BUTTONTYPE_PUSHBUTTON);
	bs.setCssClass("mgnlAdminCentralButton");

	try{
		Collection menupoints = SessionAccessControl.getHierarchyManager(request, ContentRepository.CONFIG).getContent("/modules/adminInterface/Config/menu").getChildren(ItemType.CONTENTNODE.getSystemName(), ContentHandler.SORT_BY_SEQUENCE);
		//Collection menupoints = Store.getInstance().getStore().getContent("menu").getChildren(ItemType.CONTENTNODE.getSystemName());
		for (Iterator iter = menupoints.iterator(); iter.hasNext();) {

	        Content menupoint = (Content) iter.next();
	        String label = menupoint.getNodeData("label").getString();
	        String icon  = menupoint.getNodeData("icon").getString();
	        String onclick = menupoint.getNodeData("onclick").getString();
			Button button=new Button();
			button.setLabel(MessagesManager.getWithDefault(request, label, label));
			button.setOnclick(onclick);
			button.setPushButtonTag("div");
			button.setIconSrc(request.getContextPath() + icon);

			if(first){
				button.setState(ControlSuper.BUTTONSTATE_PUSHED);
				if(iFrameSrc==""){
					firstOnClick = onclick;
				}
				first = false;
			}

			bs.setButtons(button);
	    }

	}
	catch(Exception e){
		System.out.println(e);
	}

	// get user information
	Content userPage=SessionAccessControl.getUserNode(request);
	String userName=userPage.getTitle();
	if (userName.equals("")) userName=userPage.getName();

    pageContext.setAttribute("username", userName);
    pageContext.setAttribute("buttons", bs);
    pageContext.setAttribute("firstOnClick", firstOnClick);
    pageContext.setAttribute("iFrameSrc", iFrameSrc);


</jsp:scriptlet>
    <div style="position:absolute;top:3px;right:20px;" class="mgnlText"><fmt:message key="central.loggedInAs">
        <fmt:param value="${username}" />
    </fmt:message></div>

    <jsp:scriptlet>
	if (!Server.isAdmin()) {
</jsp:scriptlet>
    <div style="position:absolute;top:20px;right:20px;" class="mgnlText"><strong>*** <fmt:message>central.publicInstance</fmt:message>
    ***</strong></div>
    <jsp:scriptlet>
	}
</jsp:scriptlet>

    <div id="mgnlAdminCentral_ButtonsDiv" class="mgnlAdminCentralButtonsDiv">

    <div class="mgnlAdminCentralButton">${buttons.html}</div>
    </div>

    <div id="mgnlAdminCentral_ExtractTreeDiv" class="mgnlAdminCentralExtractTreeDiv"><iframe
        id="mgnlAdminCentral_ExtractTreeIFrame" src="${iFrameSrc}" scrolling="no"
        style="border: none; width:100%; height:100%" frameborder="0"><![CDATA[ <!-- a comment here is needed for the correct rendering of the iframe tag -->]]></iframe>
    </div>

    <div id="mgnlAdminCentral_ExtractNonTreeDiv" class="mgnlAdminCentralExtractNonTreeDiv"><iframe
        id="mgnlAdminCentral_ExtractNonTreeIFrame" src="" scrolling="auto" style="border: none; width:100%; height:100%"
        frameborder="0"><![CDATA[ <!-- a comment here is needed for the correct rendering of the iframe tag -->]]></iframe></div>
    <SCRIPT type="text/javascript">
    	${firstOnClick}
    </SCRIPT>
    </body>
    </html>
</jsp:root>
