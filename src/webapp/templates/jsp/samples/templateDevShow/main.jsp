<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <jsp:text> <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]> </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
        <head>
            <c:import url="/templates/jsp/samples/global/head.jsp" />
        </head>
        <body>
            <c:import url="/templates/jsp/samples/global/mainBar.jsp" />
            <div id="contentDivMainColumnTotalWidth">
                <c:import url="/templates/jsp/samples/templateDevShow/columnMain.jsp"/>
                <c:import url="/templates/jsp/samples/global/footer.jsp"/>
            </div>
            <c:import url="/templates/jsp/samples/global/headerImage.jsp" />
            <c:import url="/templates/jsp/samples/global/navigation.jsp" />
        </body>
    </html>
</jsp:root>