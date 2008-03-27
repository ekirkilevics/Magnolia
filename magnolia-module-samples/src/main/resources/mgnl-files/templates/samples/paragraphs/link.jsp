<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page import="org.apache.commons.lang.StringUtils"/>
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <jsp:directive.page import="info.magnolia.cms.core.HierarchyManager" />
  <jsp:directive.page import="info.magnolia.cms.util.Resource" />
  <jsp:directive.page import="info.magnolia.cms.core.Content" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />
  <jsp:directive.page import="info.magnolia.context.MgnlContext" />
  <jsp:directive.page import="javax.jcr.RepositoryException" />
  <jsp:directive.page import="info.magnolia.cms.util.LinkUtil"/>

  <jsp:scriptlet>
  
    <![CDATA[

    String link=Resource.getLocalContentNode(request).getNodeData("link").getString();
    String text=Resource.getLocalContentNode(request).getNodeData("text").getString();

    if (!link.equals("")) {
        String linkType=Resource.getLocalContentNode(request).getNodeData("linkType").getString();
        StringBuffer html=new StringBuffer();

        html.append("&raquo; <a href=\"");

        if (linkType.equals("external")) {
            // if no protocol is defined, prepend http:// to link
            if (link.indexOf("://") < 0) {
                html.append("http://");
            }
            // open all external protocols in new window
            html.append(link+"\" target=\"_blank\">");

            if (!text.equals("")) html.append(text);
            else html.append(link);
        }
        else {
            // convert to uuid
            link = StringUtils.defaultString(LinkUtil.makeAbsolutePathFromUUID(link), link);
            html.append(request.getContextPath());
            html.append(link+".html\">");
            if (!text.equals("")) html.append(text);
            else {
                try {
                    // get title of linked page
                    HierarchyManager hm=MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                    if(hm.isExist(link)){
                        Content destinationPage=hm.getContent(link);
                        // if title is empty take the name
                        if (destinationPage.getNodeData("title").getString().equals("")) {
                            html.append(destinationPage.getName());
                        } else {
                            html.append(destinationPage.getNodeData("title").getString());
                        }
                       }
                       else{
                        html.append(link);
                    }
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