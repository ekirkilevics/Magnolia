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
%><%@ page import="info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.util.Resource,
				 info.magnolia.cms.core.ContentNode,
				 info.magnolia.cms.core.Content,
				 java.util.*,
				 org.apache.webdav.lib.WebdavResource,
				 org.apache.util.HttpURL,
				 javax.jcr.*,
				 java.lang.reflect.Array,
				 info.magnolia.cms.core.MetaData,
                 info.magnolia.cms.beans.config.Server,
                 info.magnolia.cms.beans.runtime.MultipartForm,
                 info.magnolia.cms.security.SessionAccessControl,
                 info.magnolia.cms.gui.control.Save,
                   info.magnolia.cms.exchange.simple.Syndicator,
                 info.magnolia.cms.beans.config.ContentRepository,
                 info.magnolia.cms.gui.dialog.*,
                 info.magnolia.cms.gui.control.ButtonSet,
                 info.magnolia.cms.gui.control.Button,
                 info.magnolia.cms.util.Path,
                 org.apache.log4j.Logger,
                   info.magnolia.cms.exchange.simple.Syndicator,
				   info.magnolia.cms.gui.misc.Sources,
                   info.magnolia.cms.security.Permission"%>

<%if (!Server.isAdmin()) response.sendRedirect("/");%>
<%!

    static Logger log = Logger.getLogger("user dialog");


    //todo: permission global available somewhere
	static final long PERMISSION_ALL=Permission.ALL;
	static final long PERMISSION_READ=Permission.READ;
	static final long PERMISSION_NO=0;

	static final String NODE_ACLUSERS="acl_users";
	static final String NODE_ACLROLES="acl_userroles";
	static final String NODE_ROLES="roles";
	static final String CONTROLNAME_ISADMIN_USERS="permissionUsers";
	static final String CONTROLNAME_ISADMIN_ROLES="permissionRoles";
	//static final String CONTROLNAME_ISADMIN_ACTIVATE="permissionActivate";
	static final String NODE_ACLCONFIG="acl_config";
	static final String CONTROLNAME_ISADMIN_CONFIG="permissionConfig";


%>


<%

    MultipartForm form = Resource.getPostedForm(request);
	boolean drawDialog=true;

	String path="";
	String nodeCollectionName="";
	String nodeName="";
	String paragraph="";
	String richE="";
	String richEPaste="";
	String repository;


	if (form!=null) {
		path = form.getParameter("mgnlPath");
		repository = form.getParameter("mgnlRepository");
	}
	else {
		path = request.getParameter("mgnlPath");
		repository = request.getParameter("mgnlRepository");
	}

	if (repository==null) repository=ContentRepository.USERS;

	HierarchyManager hm = new HierarchyManager(request);
	boolean create=false;
	if (path.equals("")) create=true;


    try {
        Session t = SessionAccessControl.getSession(request,repository);
        Node rootNode = t.getRootNode();
        hm.init(rootNode);
    }
    catch (Exception e) {}

	Content user=null;
	if (!create) {
		try {
			user = hm.getPage(path);
		}
		catch (RepositoryException re) {re.printStackTrace();}
	}

	if (form!=null) {
		//save

		//create new user
		if (create) {
			String name=form.getParameter("name");
			path="/"+name;
			try {
				user=hm.createPage("/",name);
			}
			catch (RepositoryException re) {
                re.printStackTrace();
            }
		}



		// ######################
		// # write to .node.xml (controls with saveInfo (full name, password))
		// ######################
		Save nodeXml=new Save(form,request);
		nodeXml.setPath(path);
		nodeXml.save();

		// ######################
		// # write users and roles acl
		// ######################

		//remove existing
		try {
			user.deleteContentNode(NODE_ACLUSERS);
		}
		catch (RepositoryException re) {}
		try {
			user.deleteContentNode(NODE_ACLROLES);
		}
		catch (RepositoryException re) {}

        try {
			user.deleteContentNode(NODE_ACLCONFIG);
		}
		catch (RepositoryException re) {}


		//rewrite
		ContentNode aclUsers=user.createContentNode(NODE_ACLUSERS);
		ContentNode aclRoles=user.createContentNode(NODE_ACLROLES);
		ContentNode aclConfig=user.createContentNode(NODE_ACLCONFIG);
		Save save=new Save();

		if (form.getParameter(CONTROLNAME_ISADMIN_USERS).equals("true")) {
			//System.out.println("IS user admin");
			//is user admin
			//full access to all users
            ContentNode u0=aclUsers.createContentNode("0");
            u0.createNodeData("path",save.getValue("/*"),PropertyType.STRING);
            u0.createNodeData("permissions",save.getValue(PERMISSION_ALL),PropertyType.LONG);
		} else {
			//not users admin
			//allow access to own user
			ContentNode u0=aclUsers.createContentNode("00");
			u0.createNodeData("path",save.getValue(user.getHandle()+"/"+NODE_ROLES),PropertyType.STRING);
			u0.createNodeData("permissions",save.getValue(PERMISSION_READ),PropertyType.LONG);

			ContentNode u1=aclUsers.createContentNode("01");
			u1.createNodeData("path",save.getValue(user.getHandle()+"/"+NODE_ROLES+"/*"),PropertyType.STRING);
			u1.createNodeData("permissions",save.getValue(PERMISSION_READ),PropertyType.LONG);

			ContentNode u2=aclUsers.createContentNode("02");
			u2.createNodeData("path",save.getValue(user.getHandle()),PropertyType.STRING);
			u2.createNodeData("permissions",save.getValue(PERMISSION_ALL),PropertyType.LONG);

			ContentNode u3=aclUsers.createContentNode("03");
			u3.createNodeData("path",save.getValue(user.getHandle()+"/*"),PropertyType.STRING);
			u3.createNodeData("permissions",save.getValue(PERMISSION_READ),PropertyType.LONG);

			ContentNode u4=aclUsers.createContentNode("04");
			u4.createNodeData("path",save.getValue("/*"),PropertyType.STRING);
			u4.createNodeData("permissions",save.getValue(PERMISSION_NO),PropertyType.LONG);
		}

		if (form.getParameter(CONTROLNAME_ISADMIN_ROLES)!=null) {
			//is roles admin:
			//full access to all roles
			ContentNode r0=aclRoles.createContentNode("0");
			r0.createNodeData("path",save.getValue("/*"),PropertyType.STRING);
			r0.createNodeData("permissions",save.getValue(PERMISSION_ALL),PropertyType.LONG);
		} else {
			//not roles admin:
			//read access to all roles
			ContentNode r0=aclRoles.createContentNode("0");
			r0.createNodeData("path",save.getValue("/*"),PropertyType.STRING);
			r0.createNodeData("permissions",save.getValue(PERMISSION_READ),PropertyType.LONG);
		}


		if (form.getParameter(CONTROLNAME_ISADMIN_CONFIG)!=null) {
			//is config admin:
			//full access to entire config repository
			ContentNode c0=aclConfig.createContentNode("0");
			c0.createNodeData("path",save.getValue("/*"),PropertyType.STRING);
			c0.createNodeData("permissions",save.getValue(PERMISSION_ALL),PropertyType.LONG);
		}
		else {
			//not config admin:
			//read access to config repository
			ContentNode c0=aclConfig.createContentNode("0");
			c0.createNodeData("path",save.getValue("/*"),PropertyType.STRING);
			c0.createNodeData("permissions",save.getValue(PERMISSION_READ),PropertyType.LONG);
		}



		// ######################
		// # roles acl
		// ######################
		//remove existing
		try {
			user.deleteContentNode(NODE_ROLES);
		}
		catch (RepositoryException re) {}

		//rewrite
		ContentNode roles=user.createContentNode(NODE_ROLES);
		String[] rolesValue=form.getParameter("aclList").split(";");
		//System.out.println(form.getParameter("aclList"));
		for (int i=0;i<rolesValue.length;i++) {
			try {
                String newLabel = Path.getUniqueLabel(hm,roles.getHandle(),"0");
				ContentNode r=roles.createContentNode(newLabel);
				r.createNodeData("path").setValue(rolesValue[i]);
			}
			catch (Exception e) {
                log.error(e.getMessage(), e);
            }
		}


        try {
            hm.save();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }


		/*
		//no automated actiovation anymore
		if (form.getParameter(CONTROLNAME_ISADMIN_ACTIVATE)!=null) {
			//activate user (value already written to .node.xml
		    //???user.getMetaData(MetaData.ACTIVATION_INFO).setUnActivated();
			try {
				Syndicator syndicator = new Syndicator(request);
				syndicator.activate(ContentRepository.USERS,"/",user.getHandle(),false);
				//activate(String context,String parent,String path,boolean recursive)
			}
			catch (Exception e) {
                log.error(e.getMessage(), e);
			}
		}
		else {
			try {
				Syndicator syndicator = new Syndicator(request);
				syndicator.deActivate(ContentRepository.USERS,user.getHandle());
			}
			catch (Exception e) {
                log.error(e.getMessage(), e);
			}
		}
		*/


		//out.println("<html><script>opener.document.location.reload();window.close();</script></html>");
		out.println("<html>");
		out.println(new Sources().getHtmlJs());
		out.println("<script>");
		//out.println("opener.mgnlUsersTree.refresh();");
		out.println("opener.mgnlTree.refresh();");
		out.println("window.close();");
		out.println("</script></html>");
		drawDialog=false;
	}
	else {
		nodeCollectionName = request.getParameter("mgnlNodeCollection");
		nodeName = request.getParameter("mgnlNode");
		paragraph = request.getParameter("mgnlParagraph");
		richE = request.getParameter("mgnlRichE");
		richEPaste = request.getParameter("mgnlRichEPaste");
	}


	if (drawDialog) {
		DialogDialog dialog=new DialogDialog();
		dialog.setRequest(request);
		dialog.setWebsiteNode(user);
		dialog.setPageContext(pageContext);
		dialog.setConfig("path",path);
		dialog.setConfig("nodeCollection",nodeCollectionName);
		dialog.setConfig("node",nodeName);
		dialog.setConfig("paragraph",paragraph);
		dialog.setConfig("richE",richE);
		dialog.setConfig("richEPaste",richEPaste);
		dialog.setConfig("repository",repository);
		dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js");


		//opener.document.location.reload();window.close();

		dialog.setConfig("width",dialog.DIALOGSIZE_SLIM_WIDTH);
		dialog.setConfig("height",dialog.DIALOGSIZE_SLIM_HEIGHT);

		if (create) dialog.setLabel("Create new user");
		else dialog.setLabel("Edit user");


		DialogTab tab=dialog.addTab();

		DialogStatic spacer=new DialogStatic();
		spacer.setConfig("line",false);

		DialogStatic lineHalf=new DialogStatic();
		lineHalf.setConfig("line",false);

		if (!create) {
			DialogStatic name=new DialogStatic();
			//name.setConfig("line",false);
			name.setLabel("<b>User name</b>");
			name.setValue("<b>"+user.getName()+"</b>");
			tab.addSub(name);
		}
		else {
			DialogEdit name=new DialogEdit();
			name.setName("name");
			name.setConfig("onchange","mgnlDialogVerifyName(this.id);");
			name.setSaveInfo(false);
            name.setLabel("<b>User name</b>");
            name.setDescription("Legal characters: a-z, 0-9, _ (underscore), - (divis)");
			tab.addSub(name);
		}

		tab.addSub(spacer);

		DialogEdit title=new DialogEdit();
		title.setName("title");
		title.setLabel("Full name");
		title.setWebsiteNode(user);
		if (create) {title.setConfig("onchange","mgnlAclSetName(this.value);");}
		tab.addSub(title);


		DialogPassword pswd=new DialogPassword();
		pswd.setName("pswd");
		pswd.setLabel("Password");
		if (!create) pswd.setConfig("labelDescription","Leave emtpy to keep existing");
		pswd.setWebsiteNode(user);
		tab.addSub(pswd);

		tab.addSub(spacer);


		/*
		DialogButtonSet activate=new DialogButtonSet();
		activate.setName(CONTROLNAME_ISADMIN_ACTIVATE);
		activate.setWebsiteNode(user);
		activate.setConfig("type",PropertyType.TYPENAME_BOOLEAN);
		activate.setButtonType(ButtonSet.BUTTONTYPE_CHECKBOX);
		Button activateButton=new Button();
		activateButton.setLabel("Permission to activate");
		activateButton.setValue("true");
		activate.addOption(activateButton);
		tab.addSub(activate);
		*/

		DialogButtonSet isUserAdmin=new DialogButtonSet();
		isUserAdmin.setName(CONTROLNAME_ISADMIN_USERS);
		isUserAdmin.setWebsiteNode(user);
		isUserAdmin.setConfig("type",PropertyType.TYPENAME_BOOLEAN);
		isUserAdmin.setConfig("lineSemi",true);
		isUserAdmin.setButtonType(ButtonSet.BUTTONTYPE_CHECKBOX);
		Button isUserAdminButton=new Button();
        isUserAdminButton.setLabel("Users administrator");
		isUserAdminButton.setValue("true");
       	isUserAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('"+isUserAdmin.getName()+"');");
		isUserAdmin.addOption(isUserAdminButton);
		tab.addSub(isUserAdmin);




		DialogButtonSet isRoleAdmin=new DialogButtonSet();
		isRoleAdmin.setName(CONTROLNAME_ISADMIN_ROLES);
		isRoleAdmin.setWebsiteNode(user);
		isRoleAdmin.setConfig("type",PropertyType.TYPENAME_BOOLEAN);
		isRoleAdmin.setConfig("lineSemi",true);
		isRoleAdmin.setButtonType(ButtonSet.BUTTONTYPE_CHECKBOX);
		Button isRolesAdminButton=new Button();
		isRolesAdminButton.setLabel("Roles administrator");
		isRolesAdminButton.setValue("true");
       	isRolesAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('"+isRoleAdmin.getName()+"');");
		isRoleAdmin.addOption(isRolesAdminButton);
		tab.addSub(isRoleAdmin);

        DialogButtonSet isConfigAdmin=new DialogButtonSet();
		isConfigAdmin.setName(CONTROLNAME_ISADMIN_CONFIG);
		isConfigAdmin.setWebsiteNode(user);
		isConfigAdmin.setConfig("type",PropertyType.TYPENAME_BOOLEAN);
		isConfigAdmin.setConfig("lineSemi",true);
		isConfigAdmin.setButtonType(ButtonSet.BUTTONTYPE_CHECKBOX);
		Button isConfigAdminButton=new Button();
		isConfigAdminButton.setLabel("Config administrator");
		isConfigAdminButton.setValue("true");
       	isConfigAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('"+isConfigAdmin.getName()+"');");
		isConfigAdmin.addOption(isConfigAdminButton);
		tab.addSub(isConfigAdmin);

		tab.addSub(spacer);


		DialogInclude roles=new DialogInclude();
		roles.setLabel("Roles");
		roles.setName("aclRolesRepository");
		roles.setWebsiteNode(user);
		roles.setConfig("file","/admintemplates/adminCentral/dialogs/usersEdit/includeRoles.jsp");
		tab.addSub(roles);

		DialogButton add=new DialogButton();
		add.setConfig("buttonLabel","Add");
		add.setConfig("lineSemi",true);
		add.setConfig("onclick","mgnlAclAdd(true,-1);");
		tab.addSub(add);



		dialog.setConfig("saveOnclick","mgnlAclFormSubmit(true);");

		dialog.drawHtml(out);
	}





%>

