<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt" xmlns:cms="urn:jsptld:cms-taglib">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <jsp:directive.page import="info.magnolia.cms.gui.dialog.DialogWebDAV" />
    <jsp:directive.page import="info.magnolia.cms.gui.dialog.DialogControlImpl" />

    <jsp:scriptlet>
    DialogWebDAV dav=(DialogWebDAV) request.getSession().getAttribute(request.getParameter(DialogControlImpl.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
    //do not remove session attribute!
    if (dav!=null) {
        if (request.getParameter("subDirectory")!=null) dav.setSubDirectory(request.getParameter("subDirectory"));
        if (request.getParameter("selectedValue")!=null) {
            dav.setValue(request.getParameter("selectedValue"));
        }
        dav.setFrameRequest(request);
        dav.drawHtmlList(out);
    }
    else {
    </jsp:scriptlet>
    <em>An error occured. Unable to connect to WebDAV Server</em>
    <jsp:scriptlet>
    }
    </jsp:scriptlet>
</jsp:root>
