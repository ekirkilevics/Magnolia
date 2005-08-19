package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.gui.dialog.DialogRichedit;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.module.admininterface.DialogPageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class RichEditorIFrameDialogPage extends DialogPageMVCHandler {

    public RichEditorIFrameDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(RichEditorIFrameDialogPage.class);

    protected void draw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        DialogRichedit richE = (DialogRichedit) request.getSession().getAttribute(
            request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
        if (richE != null) {
            richE.removeSessionAttribute();
            richE.drawHtmlEditor(out);
        }
        else {
            log.error("DialogRichedit not found in session with name [" //$NON-NLS-1$
                + request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT) + "]"); //$NON-NLS-1$
        }
    }

}
