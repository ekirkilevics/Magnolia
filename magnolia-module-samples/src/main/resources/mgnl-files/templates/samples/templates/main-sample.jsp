<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
    xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />

    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    <html>
        <head>
            <c:import url="include/header.jsp" />
        </head>
        <body>
            <h1>Add a template</h1>
            <div>
                <cms:contentNodeIterator contentNodeCollectionName="main">
                    <cms:includeTemplate />
                </cms:contentNodeIterator>
                <cms.newBar contentNodeCollectionName="main" paragraph="main-sample" />
                <c:import url="include/footer.jsp" />
            </div>
            <cmsu:simpleNavigation />
        </body>
    </html>
</jsp:root>