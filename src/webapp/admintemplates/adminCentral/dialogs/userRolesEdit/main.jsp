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
				 sun.security.provider.MD5,
				 javax.jcr.access.Permission,
				 javax.jcr.*,
				 java.lang.reflect.Array,
                 info.magnolia.cms.beans.config.Server,
                 info.magnolia.cms.beans.runtime.MultipartForm,
                 info.magnolia.cms.security.SessionAccessControl,
                 info.magnolia.cms.gui.control.Save,
                 info.magnolia.cms.gui.dialog.*,
                 info.magnolia.cms.util.Path,
                 org.apache.log4j.Logger,
                   info.magnolia.cms.beans.config.ContentRepository,
				   info.magnolia.cms.gui.misc.Sources"%>

<%if (!Server.isAdmin()) response.sendRedirect("/");%>
<%!
    static Logger log = Logger.getLogger("roles dialog");

	//todo: permission global available somewhere
	static final long PERMISSION_ALL=info.magnolia.cms.beans.runtime.Permission.ALL_PERMISSIONS;
	static final long PERMISSION_READ=Permission.READ_ITEM;
	static final long PERMISSION_NO=0;

	static final String NODE_ACL="acl_website";
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
	String repository=ContentRepository.WEBSITE;

	if (form!=null) {
		path = form.getParameter("mgnlPath");
		repository = form.getParameter("mgnlRepository");
	}
	else {
		path = request.getParameter("mgnlPath");
		repository = request.getParameter("mgnlRepository");
	}

	HierarchyManager hm = new HierarchyManager(request);
	boolean create=false;
	if (path.equals("")) create=true;


    try {
        Session t = SessionAccessControl.getSession(request,repository);
        Node rootNode = t.getRootNode();
        hm.init(rootNode);
    }
    catch (Exception e) {}

	Content role=null;
	if (!create) {
		try {
			role = hm.getPage(path);
		}
		catch (RepositoryException re) {re.printStackTrace();}
	}

	if (form!=null) {
		//save
		//create new role
		if (create) {
			String name=form.getParameter("name");
			path="/"+name;
			try {
				role=hm.createPage("/",name);
			}
			catch (RepositoryException re) {re.printStackTrace();}
		}


		// ######################
		// # write (controls with saveInfo (full name, password))
		// ######################
		Save nodeXml=new Save(form,request);
		nodeXml.setPath(path);
		nodeXml.save();


		// ######################
		// # acl
		// ######################
		//remove existing
		try {
			role.deleteContentNode(NODE_ACL);
		}
		catch (RepositoryException re) {}
		//rewrite
		ContentNode acl=role.createContentNode(NODE_ACL);
		String aclValueStr=form.getParameter("aclList");
		if (aclValueStr!=null && !aclValueStr.equals("")) {
			String[] aclValue=aclValueStr.split(";");
            for (int i=0;i<aclValue.length;i++) {
                String[] currentAclValue=aclValue[i].split(",");
                String currentPath=currentAclValue[0];
                long currentAccessRight=Long.parseLong(currentAclValue[1]);
                String currentAccessType=currentAclValue[2];

                if (currentPath.equals("/")) {
                    // needs only one entry: "/*"
                    currentAccessType="sub";
                    currentPath="";
                }

                if (currentAccessType.equals("self")) {
                    try {
                        String newLabel = Path.getUniqueLabel(hm,acl.getHandle(),"0");
                        ContentNode r=acl.createContentNode(newLabel);
                        r.createNodeData("path").setValue(currentPath);
                        r.createNodeData("permissions").setValue(currentAccessRight);
                    }
                    catch (Exception e) {e.printStackTrace();}
                }
                try {
                    String newLabel = Path.getUniqueLabel(hm,acl.getHandle(),"0");
                    ContentNode r=acl.createContentNode(newLabel);
                    r.createNodeData("path").setValue(currentPath+"/*");
                    r.createNodeData("permissions").setValue(currentAccessRight);
                }
                catch (Exception e) {e.printStackTrace();}
            }
        }
        try {
            hm.save();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }


		out.println("<html>");
		out.println(new Sources().getHtmlJs());
		out.println("<script>");
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
		dialog.setWebsiteNode(role);
		dialog.setPageContext(pageContext);
		dialog.setConfig("path",path);
		dialog.setConfig("nodeCollection",nodeCollectionName);
		dialog.setConfig("node",nodeName);
		dialog.setConfig("paragraph",paragraph);
		dialog.setConfig("richE",richE);
		dialog.setConfig("richEPaste",richEPaste);
		dialog.setConfig("repository",repository);
		dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js");

		dialog.setConfig("height",400);


		if (create) dialog.setLabel("Create new role");
		else dialog.setLabel("Edit role");
		//dialog.setConfig("saveLabel","OK");


		DialogTab tab0=dialog.addTab();
		tab0.setLabel("Properties");

		DialogTab tab1=dialog.addTab("Access control list");

		DialogStatic spacer=new DialogStatic();
		spacer.setConfig("line",false);

		if (!create) {
			DialogStatic name=new DialogStatic();
			//name.setConfig("line",false);
			name.setLabel("<b>Role name</b>");
			name.setValue("<b>"+role.getName()+"</b>");
			tab0.addSub(name);
		}
		else {
			DialogEdit name=new DialogEdit();
			name.setName("name");
			name.setLabel("<b>Role name</b>");
			name.setSaveInfo(false);
			name.setDescription("Legal characters: a-z, 0-9, _ (underscore), - (divis)");
			tab0.addSub(name);
		}

		tab0.addSub(spacer);

		DialogEdit title=new DialogEdit();
		title.setName("title");
		title.setLabel("Full name");
		title.setWebsiteNode(role);
		if (create) {title.setConfig("onchange","mgnlAclSetName(this.value);");}
		tab0.addSub(title);

		tab0.addSub(spacer);


		DialogEdit desc=new DialogEdit();
		desc.setName("description");
		desc.setLabel("Description");
		desc.setWebsiteNode(role);
		desc.setConfig("rows",6);
		tab0.addSub(desc);


		DialogInclude acl=new DialogInclude();
		acl.setBoxType(DialogBox.BOXTYPE_1COL);
		acl.setName("aclRolesRepository");
		acl.setWebsiteNode(role);
		acl.setConfig("file","/admintemplates/adminCentral/dialogs/userRolesEdit/includeAcl.jsp");
		tab1.addSub(acl);

		DialogButton add=new DialogButton();
		add.setBoxType(DialogBox.BOXTYPE_1COL);
		add.setConfig("buttonLabel","Add");
		add.setConfig("onclick","mgnlAclAdd(false,-1);");
		tab1.addSub(add);



		dialog.setConfig("saveOnclick","mgnlAclFormSubmit(false);");

		dialog.drawHtml(out);
	}





%>