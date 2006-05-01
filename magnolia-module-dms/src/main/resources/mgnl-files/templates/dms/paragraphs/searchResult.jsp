<!--
 -  Copyright 2005 obinary ag.  All rights reserved.
 -  See license distributed with this file and available
 -  online at http://www.magnolia.info/dms-license.html
 -->

<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="info.magnolia.cms.security.SessionAccessControl"%>
<%@ page import="info.magnolia.cms.core.Content"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="info.magnolia.cms.core.search.Query" %>
<%@ page import="info.magnolia.cms.core.search.QueryResult" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.obinary.magnolia.module.dms.beans.Document" %>
<%@ page import="info.magnolia.cms.util.Resource" %>

<%@taglib prefix="cms" uri="cms-taglib"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm" );
	List foundDocuments = new ArrayList();
	
	String queryStr = "SELECT * FROM nt:base WHERE " + Resource.getLocalContentNode(request).getNodeData("query").getString();
	Query q = SessionAccessControl.getQueryManager(request, "dms").createQuery(queryStr, "sql");

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
