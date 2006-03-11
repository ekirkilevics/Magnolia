package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.gui.dialog.DialogRichedit;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.module.admininterface.DialogPageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static Logger log = LoggerFactory.getLogger(RichEditorIFrameDialogPage.class);

    protected void draw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        // @todo IMPORTANT remove use of http session
        HttpSession httpsession = request.getSession(true);
        DialogRichedit richE = (DialogRichedit) httpsession.getAttribute(request
            .getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
        if (richE != null) {
            richE.removeSessionAttribute();
            richE.drawHtmlEditor(out);
        }
        else {
            log.error("DialogRichedit not found in session with name [" //$NON-NLS-1$
                + request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT)
                + "]"); //$NON-NLS-1$
        }
    }

}
