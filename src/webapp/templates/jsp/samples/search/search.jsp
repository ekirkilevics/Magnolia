<%@ page import="info.magnolia.cms.core.search.Query,
                 info.magnolia.cms.security.SessionAccessControl,
                 info.magnolia.cms.core.search.QueryResult,
                 java.util.Iterator,
                 info.magnolia.cms.core.NodeData,
                 info.magnolia.cms.core.Content"%>
<%


/**
 * Search test
 *
 *
 *
 * */
out.println("----------------------------------------------------------------------------------------<br><br>");
out.println("SQL test<br><br>");
out.println("----------------------------------------------------------------------------------------<br><br>");

String sql = "SELECT * FROM nt:base where jcr:path like '/%' and title like '%'";
out.println("Statement : "+sql+"<br><br>");

Query q = SessionAccessControl.getQueryManager(request).createQuery(sql, Query.SQL);
QueryResult result = q.execute();


out.println("<br>");

Iterator nodes  = result.getContentIterator("mgnl:content");
int i = 1;
out.println("<u>Resulting objects of NodeType (mgnl:content)</u> <br>");
while (nodes.hasNext()) {
    Content node = (Content) nodes.next();
    out.println("("+i+") "+node.getHandle()+"<br>");
    i++;
}


out.println("<br><br>");

nodes  = result.getContentIterator("mgnl:contentNode");
i = 1;
out.println("<u>Resulting objects on NodeType (mgnl:contentNode)</u> <br>");
while (nodes.hasNext()) {
    Content node = (Content) nodes.next();
    out.println("("+i+") "+node.getHandle()+"<br>");
    i++;
}


%>