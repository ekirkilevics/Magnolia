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
                 info.magnolia.cms.gui.control.Button,
                 info.magnolia.cms.gui.dialog.DialogSuper,
                 info.magnolia.cms.gui.control.Hidden,
                 info.magnolia.cms.security.SessionAccessControl,
				   info.magnolia.cms.beans.config.ContentRepository,
                   javax.jcr.Session"%>
<%!

	private static final String getHtmlRowInner(HttpServletRequest request) {
		boolean small=true;

		Button choose=new Button();
		choose.setLabel("Choose...");
		choose.setOnclick("mgnlAclChoose('"+ request.getContextPath() + "','+index+',\\\'"+ContentRepository.USER_ROLES+"\\\');");
		choose.setSmall(small);

		Button delete=new Button();
		delete.setLabel("Del");
		delete.setOnclick("mgnlAclDelete('+index+');");
		delete.setSmall(small);

		/*
		Button up=new Button();
		up.setIconSrc("/admindocroot/buttonIcons/arrowUp_small.gif");
		up.setOnclick("mgnlAclMove(true,'+index+',true);");
		up.setSmall(small);

		Button down=new Button();
		down.setIconSrc("/admindocroot/buttonIcons/arrowDown_small.gif");
		down.setOnclick("mgnlAclMove(true,'+index+',false);");
		down.setSmall(small);
		*/


		StringBuffer html=new StringBuffer();
		//set as table since ie/win does not support setting of innerHTML of a tr
		html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr style=\"height:20px\">");
		/*
		//todo: show name instead of path again (adapt linkBrowser.jsp, resp. Tree.java)
		html.append("<td id=\"acl'+index+'TdName\" width=\"100%\" class=\"mgnlDialogAcl\">'+name+'</td>");
		*/
		html.append("<td width=\"100%\" class=\""+DialogSuper.CSSCLASS_EDITWITHBUTTON+"\"><input name=\"acl'+index+'Path\" id=\"acl'+index+'Path\" class=\""+DialogSuper.CSSCLASS_EDIT+"\" type=\"text\" style=\"width:100%;\" value=\"'+path+'\"></td>");
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
%><%

	DialogSuper dialogControl=(DialogSuper) pageContext.getAttribute("dialogObject",PageContext.REQUEST_SCOPE);
	Content user=dialogControl.getWebsiteNode();

	out.println(new Hidden("aclList","",false).getHtml());


%>
<table id="aclTable" cellpadding="0" cellspacing="0" border="0" width="100%">
</table>

	<script>
		function mgnlAclGetHtmlRow(index,path,name)
			{
			return '<%=getHtmlRowInner(request)%>';
			}

			<%
			//add existing acls to table (by js, so the same mechanism as at adding rows can be used)
			try {
				ContentNode acl = user.getContentNode("roles");
				Iterator it = acl.getChildren().iterator();
				while (it.hasNext()) {
					ContentNode c = (ContentNode)it.next();
					String path = c.getNodeData("path").getString();
					String name = "";
					try {
	                    HierarchyManager hm = new HierarchyManager(request);

						try {
							Session t = SessionAccessControl.getSession(request,ContentRepository.USER_ROLES);
							Node rootNode = t.getRootNode();
							hm.init(rootNode);
						}
						catch (Exception e) {}
						Content role=null;
						try {
							role = hm.getPage(path);
							name=role.getTitle();
						}
						catch (RepositoryException re) {}
					}
					catch (Exception e) {}

					out.println("mgnlAclAdd(true,-1,'"+path+"','"+name+"');");
					//out.println("</script><br>mgnlAclAdd(-1,'"+path+"','"+name+"');<script>");
				}
			}
			catch (Exception e) {
				out.println("mgnlAclAdd(true,-1);");
			}

			%>



	</script>



