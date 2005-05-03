<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=utf-8" />

    <jsp:directive.page import="info.magnolia.cms.core.search.Query" />
    <jsp:directive.page import="info.magnolia.cms.security.SessionAccessControl" />
    <jsp:directive.page import="info.magnolia.cms.core.search.QueryResult" />
    <jsp:directive.page import="java.util.Iterator" />
    <jsp:directive.page import="info.magnolia.cms.core.NodeData" />
    <jsp:directive.page import="info.magnolia.cms.core.Content" />


    <jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
        <head>
            <c:import url="/templates/jsp/samples/global/head.jsp" />
        </head>
        <body>
            <c:import url="/templates/jsp/samples/global/mainBar.jsp" />
            <div id="contentDivMainColumn">
                <c:import url="/templates/jsp/samples/global/columnMain.jsp" />


                <form name="mgnlsearch" action="">
                  <textarea id="sql" name="sql" cols="40" rows="10">SELECT * FROM nt:base where jcr:path like '/%' and title like '%'
                  </textarea>
                  <input type="submit" name="search" value="search" />
                </form>

                <c:if test="${!empty(param.sql)}">

	                <h1>Search results for:</h1>
	                <h2>${param.sql}</h2>

						<c:catch var="exc">
			                <jsp:scriptlet>
				                String sql = request.getParameter("sql");
				                Query q = SessionAccessControl.getQueryManager(request).createQuery(sql, Query.SQL);
				                QueryResult result = q.execute();
				                pageContext.setAttribute("result", result.getContentIterator("mgnl:content"));
			                </jsp:scriptlet>

			                <h3>Resulting objects of NodeType (mgnl:content)</h3>

			                <c:forEach var="node" items="${result}">
			                  ${node.handle}<br/>
			                </c:forEach>

			                <h3>Resulting objects of NodeType (mgnl:contentNode)</h3>

			                <jsp:scriptlet>
				                pageContext.setAttribute("result", result.getContentIterator("mgnl:contentNode"));
			                </jsp:scriptlet>

			                <c:forEach var="node" items="${result}">
			                  ${node.handle}<br/>
			                </c:forEach>
						</c:catch>

						<c:if test="${!empty(exc)}">
						  	<h1>${exc.message}</h1>
						</c:if>
	                </c:if>
                       <c:import url="/templates/jsp/samples/global/footer.jsp" />
            </div>
            <div id="contentDivRightColumn">
                <c:import url="/templates/jsp/samples/global/columnRight.jsp" />
            </div>
            <c:import url="/templates/jsp/samples/global/headerImage.jsp" />
            <cmsu:simpleNavigation />
        </body>
    </html>
</jsp:root>