<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page">
    <jsp:directive.page import="info.magnolia.cms.gui.dialog.DialogRichedit" />
    <jsp:directive.page import="info.magnolia.cms.gui.dialog.DialogSuper" " />
    <jsp:scriptlet>

	DialogRichedit richE=(DialogRichedit) request.getSession().getAttribute(request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
    richE.setRequest(request);
    richE.removeSessionAttribute();
	richE.drawHtmlEditor(out);

    </jsp:scriptlet>
</jsp:root>
