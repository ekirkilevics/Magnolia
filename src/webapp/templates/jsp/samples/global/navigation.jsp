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
%><%@ page import="info.magnolia.cms.core.Content,
				 java.util.Iterator,
				 info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.util.Resource"%>



<%!
private void drawChildren(Content page,Content activePage,int indent,JspWriter out, HttpServletRequest request) {
	Iterator it=page.getChildren().iterator();
	while (it.hasNext()) {
		try {
			Content c=(Content) it.next();

			if (!c.getNodeData("hideInNav").getBoolean()) {

				StringBuffer cssClassDiv=new StringBuffer("nav");

				String title=c.getNodeData("navTitle").getString("<br>");
				//if nav title is not set, the main title is taken:
				if (title.equals("")) title=c.getTitle();
				//if main title is not set, the name of the page is taken:
				if (title.equals("")) title=c.getName();


				boolean showChildren=false;

				if (activePage.getHandle().equals(c.getHandle())) {
					//self
					showChildren=true;
					cssClassDiv.append("Self");
				}
				else {
					String cssClassA;
					if (c.getLevel()<=activePage.getAncestors().size() && activePage.getAncestor(c.getLevel()).getHandle().equals(c.getHandle())) {
						//path
						cssClassA="navPath";
						showChildren=true;
					}
					else {
						cssClassA="nav";
					}
					title="<a href=\""+request.getContextPath() + c.getHandle()+".html\" class=\""+cssClassA+"\">"+title+"</a>";
				}

				if (c.getLevel()==1) {
					cssClassDiv.append("TopLevel");
					//out.println("<tr><td class=line></td></tr>");
					out.println("<div class=\"navLine\"><br></div>");
				}
				int indentPixel=indent*10;
				//out.print("<tr><td class="+cssClassDiv+" style=padding-left:"+indentPixel+";>");
				out.print("<div class=\""+cssClassDiv+"\" style=\"padding-left:"+indentPixel+";\">");
				out.println(title);
				//out.print("</td></tr>");
				out.print("</div>");
				if (showChildren) {
					drawChildren(c,activePage,indent+1,out, request);
				}
			}
		}
		catch (Exception e) {}
	}
}
%>

<%
out.println("<div id=\"navDiv\">");
Content activePage=Resource.getActivePage(request);
Content topLevel=activePage.getAncestor(0);
drawChildren(topLevel,activePage,0,out, request);
out.println("<div class=\"navLine\"><br></div>");
out.println("</div>");
%>

