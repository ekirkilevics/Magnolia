<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
          xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=utf-8"/>
    <jsp:directive.page import="info.magnolia.cms.util.Resource"/>
    <jsp:text>
        <![CDATA[
        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <c:import url="/templates/jsp/samples/global/head.jsp"/>
    </head>

    <body>
    <c:import url="/templates/jsp/samples/templateMail/mainBarMail.jsp"/>
    <br/><br/>
    <jsp:scriptlet>
        // prepare page title
        String title = Resource.getActivePage(request).getNodeData("title").getString();
        if (title == null) {
            title = Resource.getActivePage(request).getNodeData("contentTitle").getString();
        }
        title = title.toUpperCase();
        // make it accessable for jstl
        pageContext.setAttribute("title", title);
    </jsp:scriptlet>
    <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
        <c:set var="spacer"><cms:out nodeDataName="spacer"/></c:set>
        <c:set var="lineAbove"><cms:out nodeDataName="lineAbove"/></c:set>

        <div style="clear:both;">
            <cms:adminOnly>
                <cms:editBar/>
            </cms:adminOnly>
            <!-- line -->
            <c:if test="${lineAbove=='true'}">
                <div class="line">
                    <br/>
                </div>
            </c:if>
            <cms:includeTemplate/>
        </div>

        <!-- spacer -->
        <div style="clear:both;">
            <c:if test="${spacer=='1'}">
                <br/>
            </c:if>
            <c:if test="${spacer=='2'}">
                <br/><br/>
            </c:if>
        </div>
    </cms:contentNodeIterator>

    <c:import url="/templates/jsp/samples/templateMail/mailNewBar.jsp"/>

    </body>
    </html>
</jsp:root>