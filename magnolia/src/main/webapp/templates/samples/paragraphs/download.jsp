<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib"
    xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />

    &amp;raquo;

    <cms:out nodeDataName="document" var="document" />

        <a href="${pageContext.request.contextPath}${document}" target="_blank">
            <cms:ifEmpty nodeDataName="text">
                <cms:out nodeDataName="document" fileProperty="nameWithoutExtension" />
            </cms:ifEmpty>
            <cms:ifNotEmpty nodeDataName="text">
                <cms:out nodeDataName="text" />
            </cms:ifNotEmpty>
        </a>

    &amp;ndash;
    <cms:out nodeDataName="document" fileProperty="extensionUpperCase" />-File,
    <cms:out nodeDataName="document" fileProperty="size" />

</jsp:root>


