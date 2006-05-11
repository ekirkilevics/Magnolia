<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="info.magnolia.cms.security.SessionAccessControl"%>
<%@ page import="info.magnolia.cms.core.Content"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="info.magnolia.cms.core.search.Query" %>
<%@ page import="info.magnolia.cms.core.search.QueryResult" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="info.magnolia.cms.util.Resource" %>
<%@ page import="info.magnolia.module.dms.beans.Document" %>
<%@ page import="info.magnolia.cms.beans.runtime.MgnlContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="info.magnolia.cms.core.ItemType" %>

<%@taglib prefix="cms" uri="cms-taglib"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm" );
	List foundDocuments = new ArrayList();
	
    // get the values from the current paragraph
    String link = Resource.getLocalContentNode(request).getNodeData("link").getString();
    String extension = Resource.getLocalContentNode(request).getNodeData("extension").getString();
    String query = Resource.getLocalContentNode(request).getNodeData("query").getString();
    
    List expressions = new ArrayList();

    // if a link is defined (folder or document)
    if(StringUtils.isNotEmpty(link)){
        Content node = MgnlContext.getHierarchyManager("dms").getContent(link);
        // if this is a folder search all containing documents
        if(node.getItemType().equals(ItemType.CONTENT)){
            expressions.add("jcr:path like '" + link + "/%'");
        }
        // a single file
        else if(Document.isDocument(node)){
            expressions.add("jcr:path like '" + link + "'");
        }
    }
    
    // search on file type
    if(StringUtils.isNotEmpty(extension)){
        expressions.add("extension = '" + extension + "'");
    }

    // additional query
    if(StringUtils.isNotEmpty(query)){
        expressions.add(query);
    }
    
    // join the expressions
    String where = StringUtils.join(expressions.iterator(), " and ");
    
	String queryStr= "SELECT * FROM nt:base WHERE " + where;
    
	Query q = MgnlContext.getQueryManager("dms").createQuery(queryStr, "sql");

	QueryResult result = q.execute();
	for(Iterator iter = result.getContent("mgnl:contentNode").iterator(); iter.hasNext();){
	    Content node = (Content) iter.next();
	    if(Document.isDocument(node)){
			Document doc = new Document(node);
			foundDocuments.add(
				new String[]{
					"<img border=\"0\" src=\"" + request.getContextPath() + "/" + doc.getMimeTypeIcon() + "\"(>",
					doc.getTitle() + "." + doc.getFileExtension(),
					doc.getNodeData("subject").getString(),
					dateFormat.format(doc.getModificationDate().getTime()),
					doc.getLink()
				}
			);
	    }
	}
	pageContext.setAttribute("foundDocuments", foundDocuments);
%>

<table>
<c:forEach items="${foundDocuments}" var="doc">
	<tr><td><a href="javascript:mgnl.dms.DMS.downloadFile('${doc[4]}')">${doc[0]}</a></td><td><a href="javascript:mgnl.dms.DMS.downloadFile('${doc[4]}')">${doc[1]}</a></td><td>${doc[2]}</td><td>${doc[3]}<td></tr>
</c:forEach>
</table>
