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
%><%@ page import="info.magnolia.cms.core.ContentNode,
				 java.util.Iterator,
				 javax.jcr.PathNotFoundException,
				 info.magnolia.cms.core.Content,
				 info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.util.Resource,
				 javax.jcr.Node,
				 javax.jcr.RepositoryException,
				 java.util.ArrayList,
				 javax.jcr.access.Permission,
                 info.magnolia.cms.gui.control.Select,
                 info.magnolia.cms.gui.control.Edit,
                 info.magnolia.cms.gui.control.Button,
                 info.magnolia.cms.gui.dialog.DialogSuper,
                 info.magnolia.cms.gui.control.Hidden,
				   info.magnolia.cms.beans.config.ContentRepository"%>
<%!
	//todo: permission global available somewhere
	static final long PERMISSION_ALL=info.magnolia.cms.beans.runtime.Permission.ALL_PERMISSIONS;
	static final long PERMISSION_READ=Permission.READ_ITEM;
	static final long PERMISSION_NO=0;


	private static final String getHtmlRowInner() {
		boolean small=true;

		Select accessRight=new Select();
		accessRight.setSaveInfo(false);
		accessRight.setName("acl'+index+'AccessRight");
		accessRight.setCssClass("mgnlDialogControlSelect");
		accessRight.setOptions("Read/Write",Long.toString(PERMISSION_ALL));
		accessRight.setOptions("Read only",Long.toString(PERMISSION_READ));
		accessRight.setOptions("Deny access",Long.toString(PERMISSION_NO));

		Select accessType=new Select();
		accessType.setSaveInfo(false);
		accessType.setName("acl'+index+'AccessType");
		accessType.setCssClass("mgnlDialogControlSelect");
		accessType.setOptions("Selected and sub pages","self");
		accessType.setOptions("Sub pages","sub");

		Edit path=new Edit();
		path.setSaveInfo(false);
		path.setName("acl'+index+'Path");
		path.setValue("'+path+'");
		path.setCssClass(DialogSuper.CSSCLASS_EDIT);
		path.setCssStyles("width","100%");


		Button choose=new Button();
		choose.setLabel("Choose...");
		choose.setOnclick("mgnlAclChoose('+index+',\\\'"+ContentRepository.WEBSITE+"\\\');");
		choose.setSmall(small);

		Button delete=new Button();
		delete.setLabel("Del");
		delete.setOnclick("mgnlAclDelete('+index+');");
		delete.setSmall(small);

		/*
		Button up=new Button();
		up.setIconSrc("/admindocroot/buttonIcons/arrowUp_small.gif");
		up.setOnclick("mgnlAclMove(false,'+index+',true);");
		up.setSmall(small);

		Button down=new Button();
		down.setIconSrc("/admindocroot/buttonIcons/arrowDown_small.gif");
		down.setOnclick("mgnlAclMove(false,'+index+',false);");
		down.setSmall(small);
		*/


		StringBuffer html=new StringBuffer();
		//set as table since ie/win does not support setting of innerHTML of a tr
		html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
		html.append("<td width=\"1\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+accessRight.getHtml()+"</td>");
        html.append("<td width=\"1\" class=\"mgnlDialogBoxInput\">&nbsp;</td>");
        html.append("<td width=\"1\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+accessType.getHtml()+"</td>");
        html.append("<td width=\"1\">&nbsp;</td>");
        html.append("<td width=\"100%\"class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+path.getHtml()+"</td>");
        html.append("<td width=\"1\">&nbsp;</td>");
		html.append("<td width=\"1\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+choose.getHtml()+"</td>");
		html.append("<td width=\"1\">&nbsp;</td>");
		html.append("<td width=\"1\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+delete.getHtml()+"</td>");
		//html.append("<td width=\"1\">&nbsp;</td>");
		//html.append("<td width=\"1\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+up.getHtml()+"</td>");
		//html.append("<td width=\"1\">&nbsp;</td>");
		//html.append("<td width=\"1\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\">"+down.getHtml()+"</td>");
		html.append("</tr></table>");

		return html.toString();
	}
%>
<%

	DialogSuper dialogControl=(DialogSuper) pageContext.getAttribute("dialogObject",PageContext.REQUEST_SCOPE);
	Content role=dialogControl.getWebsiteNode();

	out.println(new Hidden("aclList","",false).getHtml());


%>
<table id="aclTable" cellpadding="0" cellspacing="0" border="0" width="100%">
</table>

	<script>
		function mgnlAclGetHtmlRow(index,path,name)
			{
			return '<%=getHtmlRowInner()%>';
			}

			<%
			//add existing acls to table (by js, so the same mechanism as at adding rows can be used)
                boolean noAcl=false;
                try {
				ContentNode acl = role.getContentNode("acl_website");
                if (acl.getChildren().size()==0) noAcl=true;
				Iterator it = acl.getChildren().iterator();
				boolean skipNext=false;
				while (it.hasNext()) {
					ContentNode c = (ContentNode)it.next();

					if (skipNext) {
						skipNext=false;
					}
					else {
						String path = c.getNodeData("path").getString();
						String accessRight = c.getNodeData("permissions").getString();
						String accessType;

						if (path.indexOf("/*")==-1) {
							// access to self and subs, skip next (which is same with /*)
							skipNext=true;
							accessType="self";
						}
						else {
							if (path.equals("/*")) {
								path="/";
								accessType="self";
							}
							else {
								path=path.substring(0,path.lastIndexOf("/*"));
								accessType="sub";
							}
						}



						out.println("mgnlAclAdd(false,-1,'"+path+"','','"+accessRight+"','"+accessType+"');");
						//out.println("</script><br>mgnlAclAdd(-1,'"+path+"','"+name+"');<script>");
					}
				}
			}
			catch (Exception e) {
                noAcl = true;
			}
            if (noAcl) out.println("mgnlAclAdd(false,-1);");
			%>
	</script>



