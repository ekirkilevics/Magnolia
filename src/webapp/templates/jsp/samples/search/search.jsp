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

String sql = "SELECT * FROM mgnl:nodeData";
out.println("Statement : "+sql+"<br><br>");

Query q = SessionAccessControl.getQueryManager(request).createQuery(sql, Query.SQL);
QueryResult result = q.execute();


Iterator nodes = result.getNodeDataIterator();
int i = 1;
out.println("<u>Resulting NodeData list:</u> <br>");
while (nodes.hasNext()) {
    NodeData node = (NodeData) nodes.next();
    out.println("("+i+") "+node.getHandle()+"<br>");
    i++;
}

out.println("<br>");

nodes  = result.getContentIterator();
i = 1;
out.println("<u>Resulting Content list:</u> <br>");
while (nodes.hasNext()) {
    Content node = (Content) nodes.next();
    out.println("("+i+") "+node.getHandle()+"<br>");
    i++;
}


%>