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
				 info.magnolia.cms.gui.misc.Sources,
				 info.magnolia.cms.security.Authenticator"%><%


	//WARNING: no white spaces tolerated for save nodeData!

	int treeHeight=50;
	boolean proceed=true;

	String repository=request.getParameter("repository");
	if (repository==null || repository.equals("")) repository=ContentRepository.WEBSITE;

	String path=request.getParameter("path");
	if (path==null || path.equals("")) path="/";

	String pathOpen=request.getParameter("pathOpen");
	String pathSelected=request.getParameter("pathSelected");

	boolean create=false;
	String createItemType=ItemType.NT_NODEDATA;
	if (request.getParameter("createItemType")!=null) {
		create=true;
		createItemType=request.getParameter("createItemType");
	}


	String actionStr=request.getParameter("treeAction");
	if (actionStr!=null) {
		int action=Integer.parseInt(actionStr);
		Tree tree=new Tree(repository,request);

		if (action==Tree.ACTION_COPY || action==Tree.ACTION_MOVE) {

			String pathClipboard=request.getParameter("pathClipboard");
			int pasteType=Integer.parseInt(request.getParameter("pasteType"));

			String newPath=tree.pasteNode(pathClipboard,pathSelected,pasteType,action);
			//System.out.println("newPath: "+newPath);
			//pass new path to tree.js for selecting the newly created node
			//NOTE: tree.js checks for this pattern; adapt it there, if any changes are made here
			out.println("<input type=\"hidden\" id=\"mgnlSelectNode\" value=\""+newPath+"\">");

			if (pasteType==Tree.PASTETYPE_SUB) pathOpen=pathSelected;
			else pathOpen=pathSelected.substring(0,pathSelected.lastIndexOf("/")); //open parent path of destination path

			pathSelected=null;
		}
		else if (action==Tree.ACTION_ACTIVATE) {
			boolean recursive=false;
			if (request.getParameter("recursive")!=null) recursive=true;
			tree.activateNode(pathSelected,recursive);
			//System.out.println("activated: "+pathSelected+"; recursive: "+recursive);
		}
		else if (action==Tree.ACTION_DEACTIVATE) {
			tree.deActivateNode(pathSelected);
			//System.out.println("deActivated: "+pathSelected);
		}

	}




	String deleteNode=request.getParameter("deleteNode");
	String saveName=request.getParameter("saveName");
	StringBuffer html=new StringBuffer();

	boolean isNodeDataValue=false; //value to save is a node data's value (config admin)
	boolean isNodeDataType=false; //value to save is a node data's type (config admin)
	if (request.getParameter("isNodeDataValue")!=null && request.getParameter("isNodeDataValue").equals("true")) isNodeDataValue=true;
	if (request.getParameter("isNodeDataType")!=null && request.getParameter("isNodeDataType").equals("true")) isNodeDataType=true;

	if (saveName!=null || isNodeDataValue || isNodeDataType) {
		String value=request.getParameter("saveValue");

		if (value==null) value="";

		boolean isMeta=false; //value to save is a content's meta information
		boolean isLabel=false; //value to save is a label (name of page, content node or node data)

		if (request.getParameter("isMeta")!=null && request.getParameter("isMeta").equals("true")) isMeta=true;
		if (request.getParameter("isLabel")!=null && request.getParameter("isLabel").equals("true")) isLabel=true;

		Tree tree=new Tree(repository,request);
		if (isNodeDataValue || isNodeDataType) {
			tree.setPath(path.substring(0,path.lastIndexOf("/")));
			saveName=path.substring(path.lastIndexOf("/")+1);
		}
		else {
			tree.setPath(path);
		}

		if (isLabel) {
			html.append(tree.renameNode(value));
		}
		else if (isNodeDataType) {
			int type=Integer.valueOf(value).intValue();
			html.append(tree.saveNodeDataType(saveName,type));
		}
		else {
			html.append(tree.saveNodeData(saveName,value,isMeta));
		}

		//html will be displayed in the span as new value! therefore be carefull that treesIFrame.jsp does not create any white spaces!
		out.print(html);

		proceed=false;
	}


	if (deleteNode!=null) {
		//System.out.println("deleteNode: "+path+" - "+deleteNode);
		Tree tree=new Tree(repository,request);
		tree.deleteNode(path,deleteNode);
	}


	if (proceed) {
		boolean snippetMode=false;
		String mode=request.getParameter("treeMode");
		if (mode==null) mode="";
		if (mode.equals("snippet")) snippetMode=true;

		//tree.setShifterExpand("");
		//tree.setShifterEmpty("");

		if (!snippetMode) {
			html.append("<html><head>");
			html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
			html.append(new Sources().getHtmlJs());
			html.append(new Sources().getHtmlCss());
			html.append("</head>");

			html.append("<body class=\"mgnlBgDark\" onload=\"mgnlTree.resizeOnload();\" marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\">");

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
			websiteTree.setIconOndblclick("mgnlTreeMenuItemOpen("+websiteTree.getJavascriptTree()+");");

			websiteTree.setPath(path);

			if (create) {
				websiteTree.createNode(createItemType);
			}
			else {
				websiteTree.setPathOpen(pathOpen);
				websiteTree.setPathSelected(pathSelected);
			}

			websiteTree.addItemType(ItemType.NT_CONTENT);
			//websiteTree.addItemType(ItemType.NT_CONTENTNODE);
			//websiteTree.addItemType(ItemType.NT_NODEDATA);

			TreeColumn column0=new TreeColumn(websiteTree.getJavascriptTree());
			column0.setIsLabel(true);
			column0.setWidth(3);
            if (Server.isAdmin()) column0.setHtmlEdit();


			TreeColumn columnIcons=new TreeColumn(websiteTree.getJavascriptTree());
			columnIcons.setCssClass("");
			columnIcons.setWidth(1);
			columnIcons.setIsIcons(true);
			columnIcons.setIconsActivation(true);
			columnIcons.setIconsPermission(true);

			TreeColumn column1=new TreeColumn(websiteTree.getJavascriptTree());
			column1.setName("title");
			column1.setTitle("Title");
			column1.setWidth(2);
			if (Server.isAdmin()) column1.setHtmlEdit();

			TreeColumn column2=new TreeColumn(websiteTree.getJavascriptTree());
			column2.setName(MetaData.TEMPLATE);
			column2.setIsMeta(true);
			column2.setWidth(2);
			column2.setTitle("Template");
			Select templateSelect=new Select();
			templateSelect.setName(websiteTree.getJavascriptTree()+TreeColumn.EDIT_NAMEADDITION);
			templateSelect.setSaveInfo(false);
			templateSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);
			templateSelect.setEvent("onblur",websiteTree.getJavascriptTree()+TreeColumn.EDIT_JSSAVE);
			templateSelect.setEvent("onchange",websiteTree.getJavascriptTree()+TreeColumn.EDIT_JSSAVE);
			Iterator templates=Template.getAvailableTemplates();
			while (templates.hasNext()) {
				Template template=(Template)templates.next();
				templateSelect.setOptions(template.getName(),template.getName());
			}
			if (Server.isAdmin()) column2.setHtmlEdit(templateSelect.getHtml());
			//todo: key/value -> column2.addKeyValue("sampleBasic","Samples: Basic Template");
			//todo: preselection (set on createPage)


			TreeColumn column3=new TreeColumn(websiteTree.getJavascriptTree());
			column3.setName(MetaData.LAST_MODIFIED);
			//column3.setName(MetaData.SEQUENCE_POS);
			column3.setIsMeta(true);
			column3.setDateFormat("yy-MM-dd, HH:mm");
			column3.setWidth(2);
			column3.setTitle("Mod. date");


			websiteTree.addColumn(column0);
			websiteTree.addColumn(column1);
			if (Server.isAdmin()) {
				websiteTree.addColumn(columnIcons);
			}
			websiteTree.addColumn(column2);
			websiteTree.addColumn(column3);



			TreeMenuItem menuOpen=new TreeMenuItem();
			menuOpen.setLabel("Open page...");
			menuOpen.setOnclick("mgnlTreeMenuItemOpen("+websiteTree.getJavascriptTree()+");");
			menuOpen.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuNewPage=new TreeMenuItem();
			menuNewPage.setLabel("New page");
			menuNewPage.setOnclick(websiteTree.getJavascriptTree()+".createNode('"+ItemType.NT_CONTENT+"');");
			menuNewPage.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

			TreeMenuItem menuDelete=new TreeMenuItem();
			menuDelete.setLabel("Delete");
			menuDelete.setOnclick(websiteTree.getJavascriptTree()+".deleteNode();");
			menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

			TreeMenuItem menuMove=new TreeMenuItem();
			menuMove.setLabel("Move page");
			menuMove.setOnclick(websiteTree.getJavascriptTree()+".cutNode();");
			menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

			TreeMenuItem menuCopy=new TreeMenuItem();
			menuCopy.setLabel("Copy page");
            menuCopy.setOnclick(websiteTree.getJavascriptTree()+".copyNode();");
			menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuActivateExcl=new TreeMenuItem();
			menuActivateExcl.setLabel("Activate this page");
			menuActivateExcl.setOnclick(websiteTree.getJavascriptTree()+".activateNode("+Tree.ACTION_ACTIVATE+",false);");
			menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

			TreeMenuItem menuActivateIncl=new TreeMenuItem();
			menuActivateIncl.setLabel("Activate incl. sub pages");
			menuActivateIncl.setOnclick(websiteTree.getJavascriptTree()+".activateNode("+Tree.ACTION_ACTIVATE+",true);");
			menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

			TreeMenuItem menuDeActivate=new TreeMenuItem();
			menuDeActivate.setLabel("De-activate");
			menuDeActivate.setOnclick(websiteTree.getJavascriptTree()+".deActivateNode("+Tree.ACTION_DEACTIVATE+");");
			menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

			TreeMenuItem menuRefresh=new TreeMenuItem();
			menuRefresh.setLabel("Refresh");
			menuRefresh.setOnclick(websiteTree.getJavascriptTree()+".refresh();");

			websiteTree.addMenuItem(menuOpen);
			websiteTree.addMenuItem(null); //line
			if (Server.isAdmin()) {
				websiteTree.addMenuItem(menuNewPage);
			}
			websiteTree.addMenuItem(menuDelete);
			if (Server.isAdmin()) {
				websiteTree.addMenuItem(null); //line
				websiteTree.addMenuItem(menuCopy);
				websiteTree.addMenuItem(menuMove);
				websiteTree.addMenuItem(null); //line
				websiteTree.addMenuItem(menuActivateExcl);
				websiteTree.addMenuItem(menuActivateIncl);
				websiteTree.addMenuItem(menuDeActivate);
			}
			websiteTree.addMenuItem(null); //line
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
			if (Server.isAdmin()) usersTree.setIconOndblclick("mgnlTreeMenuOpenDialog("+usersTree.getJavascriptTree()+",'/.magnolia/adminCentral/users/dialog.html');");

			usersTree.addItemType(ItemType.NT_CONTENT);

			if (create) {
				usersTree.createNode(createItemType);
			}
			else {
				usersTree.setPathOpen(pathOpen);
				usersTree.setPathSelected(pathSelected);
			}



			TreeColumn column0=new TreeColumn(usersTree.getJavascriptTree());
			column0.setIsLabel(true);
			if (Server.isAdmin()) column0.setHtmlEdit();
			column0.setTitle("User name");
			column0.setWidth(2);

			TreeColumn column1=new TreeColumn(usersTree.getJavascriptTree());
			column1.setName("title");
			if (Server.isAdmin()) column1.setHtmlEdit();
			column1.setTitle("Full name");
			column1.setWidth(2);

			TreeColumn columnIcons=new TreeColumn(usersTree.getJavascriptTree());
			columnIcons.setCssClass("");
			columnIcons.setWidth(1);
			columnIcons.setIsIcons(true);
			columnIcons.setIconsActivation(true);


			TreeColumn column2=new TreeColumn(usersTree.getJavascriptTree());
			column2.setName(MetaData.LAST_MODIFIED);
			column2.setIsMeta(true);
			column2.setDateFormat("yyyy-MM-dd, HH:mm");
			column2.setTitle("Mod. date");
			column2.setWidth(2);

			usersTree.addColumn(column0);
			usersTree.addColumn(column1);
			if (Server.isAdmin()) {
				usersTree.addColumn(columnIcons);
			}
			usersTree.addColumn(column2);


			TreeMenuItem menuOpen=new TreeMenuItem();
			menuOpen.setLabel("Edit user...");
			menuOpen.setOnclick("mgnlTreeMenuOpenDialog("+usersTree.getJavascriptTree()+",'/.magnolia/adminCentral/users/dialog.html');");
			menuOpen.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuNewPage=new TreeMenuItem();
			menuNewPage.setLabel("New user");
			menuNewPage.setOnclick(usersTree.getJavascriptTree()+".createRootNode('"+ItemType.NT_CONTENT+"');");

			TreeMenuItem menuDelete=new TreeMenuItem();
			menuDelete.setLabel("Delete");
			menuDelete.setOnclick(usersTree.getJavascriptTree()+".deleteNode();");
			menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuMove=new TreeMenuItem();
			menuMove.setLabel("Move user");
			menuMove.setOnclick(usersTree.getJavascriptTree()+".cutNode();");
			menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuCopy=new TreeMenuItem();
			menuCopy.setLabel("Copy user");
            menuCopy.setOnclick(usersTree.getJavascriptTree()+".copyNode();");
			menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");


			TreeMenuItem menuActivateExcl=new TreeMenuItem();
			menuActivateExcl.setLabel("Activate user");
			menuActivateExcl.setOnclick(usersTree.getJavascriptTree()+".activateNode("+Tree.ACTION_ACTIVATE+",false);");
			menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuDeActivate=new TreeMenuItem();
			menuDeActivate.setLabel("De-activate user");
			menuDeActivate.setOnclick(usersTree.getJavascriptTree()+".deActivateNode("+Tree.ACTION_DEACTIVATE+");");
			menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuRefresh=new TreeMenuItem();
			menuRefresh.setLabel("Refresh");
			menuRefresh.setOnclick(usersTree.getJavascriptTree()+".refresh();");

			if (Server.isAdmin()) {
				usersTree.addMenuItem(menuOpen);
				usersTree.addMenuItem(null); //line
				usersTree.addMenuItem(menuNewPage);
			}
			usersTree.addMenuItem(menuDelete);
			if (Server.isAdmin()) {
				usersTree.addMenuItem(null); //line
				usersTree.addMenuItem(menuActivateExcl);
				usersTree.addMenuItem(menuDeActivate);
			}
			usersTree.addMenuItem(null); //line
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
			if (Server.isAdmin()) rolesTree.setIconOndblclick("mgnlTreeMenuOpenDialog("+rolesTree.getJavascriptTree()+",'/.magnolia/adminCentral/userRoles/dialog.html');");

			rolesTree.addItemType(ItemType.NT_CONTENT);


			if (create) {
				rolesTree.createNode(createItemType);
			}
			else {
				rolesTree.setPathOpen(pathOpen);
				rolesTree.setPathSelected(pathSelected);
			}


			TreeColumn column0=new TreeColumn(rolesTree.getJavascriptTree());
			column0.setIsLabel(true);
			if (Server.isAdmin()) column0.setHtmlEdit();
			column0.setWidth(2);
			column0.setTitle("Role name");

			TreeColumn column1=new TreeColumn(rolesTree.getJavascriptTree());
			column1.setName("title");
			if (Server.isAdmin()) column1.setHtmlEdit();
			column1.setWidth(2);
			column1.setTitle("Full role name");

			TreeColumn columnIcons=new TreeColumn(rolesTree.getJavascriptTree());
			columnIcons.setCssClass("");
			columnIcons.setWidth(1);
			columnIcons.setIsIcons(true);
			columnIcons.setIconsActivation(true);

			TreeColumn column2=new TreeColumn(rolesTree.getJavascriptTree());
			column2.setName(MetaData.CREATION_DATE);
			//column2.setName(MetaData.SEQUENCE_POS);
			column2.setIsMeta(true);
			column2.setDateFormat("yyyy-MM-dd, HH:mm");
			column2.setTitle("Mod. date");
			column2.setWidth(2);

			rolesTree.addColumn(column0);
			rolesTree.addColumn(column1);
			if (Server.isAdmin()) {
				rolesTree.addColumn(columnIcons);
			}
			rolesTree.addColumn(column2);

			TreeMenuItem menuOpen=new TreeMenuItem();
			menuOpen.setLabel("Edit role...");
			menuOpen.setOnclick("mgnlTreeMenuOpenDialog("+rolesTree.getJavascriptTree()+",'/.magnolia/adminCentral/userRoles/dialog.html');");
			menuOpen.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuNewPage=new TreeMenuItem();
			menuNewPage.setLabel("New role");
			menuNewPage.setOnclick(rolesTree.getJavascriptTree()+".createRootNode('"+ItemType.NT_CONTENT+"');");

			TreeMenuItem menuDelete=new TreeMenuItem();
			menuDelete.setLabel("Delete");
			menuDelete.setOnclick(rolesTree.getJavascriptTree()+".deleteNode();");
			menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuMove=new TreeMenuItem();
			menuMove.setLabel("Move role");
			menuMove.setOnclick(rolesTree.getJavascriptTree()+".cutNode();");
			menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuCopy=new TreeMenuItem();
			menuCopy.setLabel("Copy role");
            menuCopy.setOnclick(rolesTree.getJavascriptTree()+".copyNode();");
			menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");


			TreeMenuItem menuRefresh=new TreeMenuItem();
			menuRefresh.setLabel("Refresh");
			menuRefresh.setOnclick(rolesTree.getJavascriptTree()+".refresh();");


			TreeMenuItem menuActivateExcl=new TreeMenuItem();
			menuActivateExcl.setLabel("Activate role");
			menuActivateExcl.setOnclick(rolesTree.getJavascriptTree()+".activateNode("+Tree.ACTION_ACTIVATE+",false);");
			menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuDeActivate=new TreeMenuItem();
			menuDeActivate.setLabel("De-activate role");
			menuDeActivate.setOnclick(rolesTree.getJavascriptTree()+".deActivateNode("+Tree.ACTION_DEACTIVATE+");");
			menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");



			if (Server.isAdmin()) {
				rolesTree.addMenuItem(menuOpen);
				rolesTree.addMenuItem(null); //line
				rolesTree.addMenuItem(menuNewPage);
			}
			rolesTree.addMenuItem(menuDelete);
			if (Server.isAdmin()) {
				rolesTree.addMenuItem(null); //line
				rolesTree.addMenuItem(menuActivateExcl);
				rolesTree.addMenuItem(menuDeActivate);
			}
			rolesTree.addMenuItem(null); //line
			rolesTree.addMenuItem(menuRefresh);



			String display="none";
			if (repository.equals(ContentRepository.USER_ROLES)) display="block";

			if (!snippetMode) out.println("<div id="+rolesTree.getJavascriptTree()+"_DivSuper style=\"display:"+display+";\">");
			out.print(rolesTree.getHtml());  //print, not println! because of snippet mode!
			if (!snippetMode) out.println("</div>");
		}



		//if (!snippetMode || repository.equals(ContentRepository.CONFIG)) {
		if (repository.equals(ContentRepository.CONFIG)) {
			Tree configTree=new Tree(ContentRepository.CONFIG,request);
			//configTree.setJavascriptTree("mgnlConfigTree");
			configTree.setJavascriptTree("mgnlTree");
			configTree.setSnippetMode(snippetMode);
			configTree.setHeight(treeHeight);
			configTree.setIconPage(Tree.ICONDOCROOT+"folder_cubes.gif");

			configTree.setPath(path);

			if (create) {
				configTree.createNode(createItemType);
			}
			else {
				configTree.setPathOpen(pathOpen);
				configTree.setPathSelected(pathSelected);
			}


			configTree.addItemType(ItemType.NT_CONTENT);
			configTree.addItemType(ItemType.NT_CONTENTNODE);
			configTree.addItemType(ItemType.NT_NODEDATA);


			TreeColumn column0=new TreeColumn(configTree.getJavascriptTree());
			column0.setWidth(1);
			column0.setHtmlEdit();
			column0.setIsLabel(true);
			column0.setWidth(3);

			TreeColumn column1=new TreeColumn(configTree.getJavascriptTree());
			column1.setName("");
			column1.setTitle("Value");
			column1.setIsNodeDataValue(true);
			column1.setWidth(2);
			column1.setHtmlEdit();

			TreeColumn column2=new TreeColumn(configTree.getJavascriptTree());
			column2.setName("");
			column2.setTitle("Type");
			column2.setIsNodeDataType(true);
			column2.setWidth(2);
			Select typeSelect=new Select();
			typeSelect.setName(configTree.getJavascriptTree()+TreeColumn.EDIT_NAMEADDITION);
			typeSelect.setSaveInfo(false);
			typeSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);
			typeSelect.setEvent("onblur",configTree.getJavascriptTree()+TreeColumn.EDIT_JSSAVE);
			typeSelect.setOptions(PropertyType.TYPENAME_STRING,Integer.toString(PropertyType.STRING));
			typeSelect.setOptions(PropertyType.TYPENAME_BOOLEAN,Integer.toString(PropertyType.BOOLEAN));
			typeSelect.setOptions(PropertyType.TYPENAME_LONG,Integer.toString(PropertyType.LONG));
			typeSelect.setOptions(PropertyType.TYPENAME_DOUBLE,Integer.toString(PropertyType.DOUBLE));
			//todo: typeSelect.setOptions(PropertyType.TYPENAME_DATE,Integer.toString(PropertyType.DATE));
			column2.setHtmlEdit(typeSelect.getHtml());


			TreeColumn columnIcons=new TreeColumn(configTree.getJavascriptTree());
			columnIcons.setCssClass("");
			columnIcons.setWidth(1);
			columnIcons.setIsIcons(true);
			columnIcons.setIconsActivation(true);
			columnIcons.setIconsPermission(true);

			TreeColumn column4=new TreeColumn(configTree.getJavascriptTree());
			column4.setName(MetaData.LAST_MODIFIED);
			column4.setIsMeta(true);
			column4.setDateFormat("yy-MM-dd, HH:mm");
			column4.setWidth(2);
			column4.setTitle("Mod. date");


			configTree.addColumn(column0);
			configTree.addColumn(column1);
			configTree.addColumn(column2);
			if (Server.isAdmin()) {
				configTree.addColumn(columnIcons);
			}
			configTree.addColumn(column4);



			/*
			//dev
			TreeColumn column3=new TreeColumn(configTree.getJavascriptTree());
			column3.setName(MetaData.SEQUENCE_POS);
			column3.setIsMeta(true);
			column3.setWidth(2);
			column3.setTitle("SEQ_POS");
			configTree.addColumn(column3);
			*/


			TreeMenuItem menuNewPage=new TreeMenuItem();
			menuNewPage.setLabel("<img src=\""+new Icon().getSrc(Icon.PAGE,Icon.SIZE_SMALL)+"\"> <span style=\"position:relative;top:-3px;\">New folder</span>");
			menuNewPage.setOnclick(configTree.getJavascriptTree()+".createNode('"+ItemType.NT_CONTENT+"');");
			menuNewPage.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
			menuNewPage.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotContentNode");

			TreeMenuItem menuNewContentNode=new TreeMenuItem();
			menuNewContentNode.setLabel("<img src=\""+new Icon().getSrc(Icon.CONTENTNODE,Icon.SIZE_SMALL)+"\"> <span style=\"position:relative;top:-3px\">New content node</span>");
			menuNewContentNode.setOnclick(configTree.getJavascriptTree()+".createNode('"+ItemType.NT_CONTENTNODE+"');");
			menuNewContentNode.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");

			TreeMenuItem menuNewNodeData=new TreeMenuItem();
			menuNewNodeData.setLabel("<img src=\""+new Icon().getSrc(Icon.NODEDATA,Icon.SIZE_SMALL)+"\"> <span style=\"position:relative;top:-3px;\">New node data</span>");
			menuNewNodeData.setOnclick(configTree.getJavascriptTree()+".createNode('"+ItemType.NT_NODEDATA+"');");
			menuNewNodeData.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");

			TreeMenuItem menuDelete=new TreeMenuItem();
			menuDelete.setLabel("Delete");
			//menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");
			menuDelete.setOnclick(configTree.getJavascriptTree()+".deleteNode();");
			menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

			TreeMenuItem menuCopy=new TreeMenuItem();
			menuCopy.setLabel("Copy");
			menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuCopy.setOnclick(configTree.getJavascriptTree()+".copyNode();");

			TreeMenuItem menuCut=new TreeMenuItem();
			menuCut.setLabel("Move");
			menuCut.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuCut.setOnclick(configTree.getJavascriptTree()+".cutNode();");

			TreeMenuItem menuActivateExcl=new TreeMenuItem();
			menuActivateExcl.setLabel("Activate this node");
			menuActivateExcl.setOnclick(configTree.getJavascriptTree()+".activateNode("+Tree.ACTION_ACTIVATE+",false);");
			menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");

			TreeMenuItem menuActivateIncl=new TreeMenuItem();
			menuActivateIncl.setLabel("Activate incl. sub nodes");
			menuActivateIncl.setOnclick(configTree.getJavascriptTree()+".activateNode("+Tree.ACTION_ACTIVATE+",true);");
			menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");

			TreeMenuItem menuDeActivate=new TreeMenuItem();
			menuDeActivate.setLabel("De-activate");
			menuDeActivate.setOnclick(configTree.getJavascriptTree()+".deActivateNode("+Tree.ACTION_DEACTIVATE+");");
			menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
			menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");

			TreeMenuItem menuRefresh=new TreeMenuItem();
			menuRefresh.setLabel("Refresh");
			menuRefresh.setOnclick(configTree.getJavascriptTree()+".refresh();");

			configTree.addMenuItem(menuNewPage);
			configTree.addMenuItem(menuNewContentNode);
			configTree.addMenuItem(menuNewNodeData);
			configTree.addMenuItem(null); //line
			configTree.addMenuItem(menuDelete);
			configTree.addMenuItem(null); //line
			configTree.addMenuItem(menuCopy);
			configTree.addMenuItem(menuCut);
			if (Server.isAdmin()) {
				configTree.addMenuItem(null); //line
				configTree.addMenuItem(menuActivateExcl);
				configTree.addMenuItem(menuActivateIncl);
				configTree.addMenuItem(menuDeActivate);
			}
			configTree.addMenuItem(null); //line
			configTree.addMenuItem(menuRefresh);

			String display="none";
			if (repository.equals(ContentRepository.CONFIG)) display="block";

			if (!snippetMode) out.println("<div id="+configTree.getJavascriptTree()+"_DivSuper style=\"display:"+display+";\">");
			out.print(configTree.getHtml());  //print, not println! because of snippet mode!
			if (!snippetMode) out.println("</div>");
		}



		if (!snippetMode) {
			out.println("</body></html>");
			}
		}


		//WARNING: no white spaces below! %>