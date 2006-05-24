<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <jsp:directive.page import="info.magnolia.cms.util.Resource" />
  <jsp:directive.page import="info.magnolia.cms.core.HierarchyManager" />
  <jsp:directive.page import="info.magnolia.cms.core.Content" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />
  <jsp:directive.page import="info.magnolia.context.MgnlContext" />
  <jsp:directive.page import="javax.jcr.RepositoryException" />
  <jsp:scriptlet>
    <![CDATA[

    String link=Resource.getLocalContentNode(request).getNodeData("link").getString();
    String text=Resource.getLocalContentNode(request).getNodeData("text").getString();

    if (!link.equals("")) {
        String linkType=Resource.getLocalContentNode(request).getNodeData("linkType").getString();
        StringBuffer html=new StringBuffer();

        html.append("&raquo; <a href=\"");

        if (linkType.equals("external")) {
            //if no protocol is defined, add http:// to link
            if (html.indexOf("://")==-1) html.append("http://"+link+"\" target=\"_blank\">");
            if (!text.equals("")) html.append(text);
            else html.append(link);
        }
        else {
            html.append(request.getContextPath());
            html.append(link+".html\">");
            if (!text.equals("")) html.append(text);
            else {
                try {
                    // get title of linked page
                    HierarchyManager hm=MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                    Content destinationPage=hm.getContent(link);
                    html.append(destinationPage.getNodeData("title").getString());
                }
                catch (RepositoryException re) {
                    html.append(link);
                }
            }
        }
        html.append("</a>");

        out.println(html);
    }
]]>
  </jsp:scriptlet>
</jsp:root>