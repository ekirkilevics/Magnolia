package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.dialog.DialogWebDAV;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.DialogPageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class WebDavIFrameDialogPage extends DialogPageMVCHandler {

    public WebDavIFrameDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    protected void draw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        DialogWebDAV dav = (DialogWebDAV) request.getSession().getAttribute(
            request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
        // do not remove session attribute!
        if (dav != null) {
            if (request.getParameter("subDirectory") != null) { //$NON-NLS-1$
                dav.setSubDirectory(request.getParameter("subDirectory")); //$NON-NLS-1$
            }
            if (request.getParameter("selectedValue") != null) { //$NON-NLS-1$
                dav.setValue(request.getParameter("selectedValue")); //$NON-NLS-1$
            }
            dav.drawHtmlList(out);
        }
        else {
            out.println("<i>" + MessagesManager.get(request, "webdav.error") + "</i>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

    }

}
