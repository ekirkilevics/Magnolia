package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.dialog.DialogWebDAV;
import info.magnolia.cms.i18n.ContextMessages;
import info.magnolia.cms.servlets.BasePageServlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class WebDavIFrameDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {
        PrintWriter out = response.getWriter();

        DialogWebDAV dav = (DialogWebDAV) request.getSession().getAttribute(
            request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
        // do not remove session attribute!
        if (dav != null) {
            if (request.getParameter("subDirectory") != null)
                dav.setSubDirectory(request.getParameter("subDirectory"));
            if (request.getParameter("selectedValue") != null) {
                dav.setValue(request.getParameter("selectedValue"));
            }
            dav.drawHtmlList(out);
        }
        else {
            out.println("<i>" + ContextMessages.get(request, "webdav.error") + "</i>");
        }

    }

}
