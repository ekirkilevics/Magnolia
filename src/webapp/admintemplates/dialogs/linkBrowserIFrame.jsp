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
%><%@ page import="java.util.Vector,
				 java.util.ArrayList,
				 info.magnolia.cms.beans.config.ItemType,
				 info.magnolia.cms.gui.control.TreeColumn,
				 info.magnolia.cms.gui.control.Tree,
				 info.magnolia.cms.beans.config.ContentRepository,
				 info.magnolia.cms.gui.dialog.DialogSpacer,
				 info.magnolia.cms.core.MetaData,
				 info.magnolia.cms.gui.control.TreeMenuItem,
				 info.magnolia.cms.gui.control.Select,
				 javax.jcr.PropertyType,
				 info.magnolia.cms.beans.config.Template,
				 java.util.Iterator,
				 info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.security.SessionAccessControl,
				 javax.jcr.RepositoryException,
				 info.magnolia.cms.core.Content,
				 info.magnolia.cms.core.ContentNode,
				 info.magnolia.cms.gui.misc.Icon,
				 info.magnolia.cms.exchange.simple.Syndicator,
				 java.net.URLDecoder,
				 info.magnolia.cms.beans.config.Server,
				 java.net.URLEncoder,
				 info.magnolia.cms.gui.misc.Sources"%><%


	//WARNING: no white spaces tolerated for save nodeData!

	int treeHeight=50;

	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	if (path==null || path.equals("")) path="/";

	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");


	StringBuffer html=new StringBuffer();

	boolean isNodeDataValue=false; //value to save is a node data's value (config admin)
	boolean isNodeDataType=false; //value to save is a node data's type (config admin)
	if (request.getParameter("isNodeDataValue")!=null && request.getParameter("isNodeDataValue").equals("true")) isNodeDataValue=true;
	if (request.getParameter("isNodeDataType")!=null && request.getParameter("isNodeDataType").equals("true")) isNodeDataType=true;



	boolean snippetMode=false;
	String mode=request.getParameter("treeMode");
	if (mode==null) mode="";
	if (mode.equals("snippet")) snippetMode=true;

	//tree.setShifterExpand("");
	//tree.setShifterEmpty("");

	if (!snippetMode) {
		html.append("<html><head>");
		html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
		html.append(new Sources(request.getContextPath()).getHtmlJs());
		html.append(new Sources(request.getContextPath()).getHtmlCss());
		html.append("</head>");

		html.append("<body class=\"mgnlBgDark\" onload=\"mgnlTree.resize();\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">");

		html.append(new DialogSpacer().getHtml(20));

		out.println(html);

	}




	//if (!snippetMode || repository.equals(ContentRepository.WEBSITE)) {
	if (repository.equals(ContentRepository.WEBSITE)) {
		Tree websiteTree=new Tree(ContentRepository.WEBSITE,request);
		//websiteTree.setJavascriptTree("mgnlWebsiteTree");
		websiteTree.setJavascriptTree("mgnlTree");
		websiteTree.setSnippetMode(snippetMode);
		websiteTree.setHeight(treeHeight);

		websiteTree.setPath(path);

		websiteTree.setPathOpen(pathOpen);
		websiteTree.setPathSelected(pathSelected);

		websiteTree.addItemType(ItemType.NT_CONTENT);

		TreeColumn column0=new TreeColumn(websiteTree.getJavascriptTree(), request);
		column0.setIsLabel(true);
		column0.setWidth(3);


		TreeColumn column1=new TreeColumn(websiteTree.getJavascriptTree(), request);
		column1.setName("title");
		column1.setTitle("Title");
		column1.setWidth(2);


		websiteTree.addColumn(column0);
		websiteTree.addColumn(column1);



		TreeMenuItem menuRefresh=new TreeMenuItem();
		menuRefresh.setLabel("Refresh");
		menuRefresh.setOnclick(websiteTree.getJavascriptTree()+".refresh();");

		websiteTree.addMenuItem(menuRefresh);



		String display="none";
		if (repository.equals(ContentRepository.WEBSITE)) display="block";

		if (!snippetMode) out.println("<div id="+websiteTree.getJavascriptTree()+"_DivSuper style=\"display:"+display+";\">");
		out.print(websiteTree.getHtml()); //print, not println! because of snippet mode!
		if (!snippetMode) out.println("</div>");

	}



	//if (!snippetMode || repository.equals(ContentRepository.USERS)) {
	if (repository.equals(ContentRepository.USERS)) {
		Tree usersTree=new Tree(ContentRepository.USERS,request);
		//usersTree.setJavascriptTree("mgnlUsersTree");
		usersTree.setJavascriptTree("mgnlTree");
		usersTree.setSnippetMode(snippetMode);
		usersTree.setHeight(treeHeight);
		usersTree.setDrawShifter(false);
		usersTree.setIconPage(Tree.ICONDOCROOT+"pawn_glass_yellow.gif");

		usersTree.addItemType(ItemType.NT_CONTENT);

		usersTree.setPathOpen(pathOpen);
		usersTree.setPathSelected(pathSelected);


		TreeColumn column0=new TreeColumn(usersTree.getJavascriptTree(), request);
		column0.setIsLabel(true);
		column0.setTitle("User name");
		column0.setWidth(2);

		TreeColumn column1=new TreeColumn(usersTree.getJavascriptTree(), request);
		column1.setName("title");
		column1.setTitle("Full name");
		column1.setWidth(2);

		usersTree.addColumn(column0);
		usersTree.addColumn(column1);


		TreeMenuItem menuRefresh=new TreeMenuItem();
		menuRefresh.setLabel("Refresh");
		menuRefresh.setOnclick(usersTree.getJavascriptTree()+".refresh();");

		usersTree.addMenuItem(menuRefresh);


		String display="none";
		if (repository.equals(ContentRepository.USERS)) display="block";

		if (!snippetMode) out.println("<div id="+usersTree.getJavascriptTree()+"_DivSuper style=\"display:"+display+";\">");
		out.print(usersTree.getHtml()); //print, not println! because of snippet mode!
		if (!snippetMode) out.println("</div>");
	}


	//if (!snippetMode || repository.equals(ContentRepository.USER_ROLES)) {
	if (repository.equals(ContentRepository.USER_ROLES)) {
		Tree rolesTree=new Tree(ContentRepository.USER_ROLES,request);
		//rolesTree.setJavascriptTree("mgnlUserRolesTree");
		rolesTree.setJavascriptTree("mgnlTree");
		rolesTree.setSnippetMode(snippetMode);
		rolesTree.setHeight(treeHeight);
		rolesTree.setDrawShifter(false);
		rolesTree.setIconPage(Tree.ICONDOCROOT+"hat_white.gif");

		rolesTree.addItemType(ItemType.NT_CONTENT);


		rolesTree.setPathOpen(pathOpen);
		rolesTree.setPathSelected(pathSelected);


		TreeColumn column0=new TreeColumn(rolesTree.getJavascriptTree(), request);
		column0.setIsLabel(true);
		column0.setWidth(2);
		column0.setTitle("name");

		TreeColumn column1=new TreeColumn(rolesTree.getJavascriptTree(), request);
		column1.setName("title");
		column1.setWidth(2);
		column1.setTitle("Full role name");

		rolesTree.addColumn(column0);
		rolesTree.addColumn(column1);

		TreeMenuItem menuRefresh=new TreeMenuItem();
		menuRefresh.setLabel("Refresh");
		menuRefresh.setOnclick(rolesTree.getJavascriptTree()+".refresh();");

		rolesTree.addMenuItem(menuRefresh);



		String display="none";
		if (repository.equals(ContentRepository.USER_ROLES)) display="block";

		if (!snippetMode) out.println("<div id="+rolesTree.getJavascriptTree()+"_DivSuper style=\"display:"+display+";\">");
		out.print(rolesTree.getHtml());  //print, not println! because of snippet mode!
		if (!snippetMode) out.println("</div>");
	}


	if (!snippetMode) {
		out.println("</body></html>");
		}


		//WARNING: no white spaces below! %>