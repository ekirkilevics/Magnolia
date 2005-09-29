package info.magnolia.cms.servlets;

import info.magnolia.cms.i18n.MessagesManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Simple servlet to reload resource bundles and thus update the content of the internationalization
 * @author niko
 */
public class ReloadI18nServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * request parameter: reload requested.
     */
    private static final String PARAM_RELOAD_ACTION = "reload"; //$NON-NLS-1$

    /**
     * access the MessagesManager and ask him to reload the bundles
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        displayReloadForm(request, out);
        out.flush();
        out.close();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        PrintWriter out = response.getWriter();
        displayReloadForm(request, out);
        out.println("----------------------<br/>"); //$NON-NLS-1$
        executeReload(request, out);
        out.flush();
        out.close();
    }

    private void executeReload(HttpServletRequest request, PrintWriter out) {
        try {
            MessagesManager.getMessages(request, null, MessagesManager.getDefaultLocale()).reloadBundles();
            out
                .println(MessagesManager.get(request, "reloadi18n.reloaded") + ":" + new SimpleDateFormat().format(new Date()) + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (Exception e) {
            out.println(MessagesManager.get(request, "reloadi18n.error") + "<br/><br/>"); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     * Display a simple form for reloading data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     */
    private void displayReloadForm(HttpServletRequest request, PrintWriter out) {

        out.println("<html><head><title>Magnolia</title>"); //$NON-NLS-1$
        // @todo FIXME! out.println(new Sources(request.getContextPath()).getHtmlCss());
        out.println("</head><body class=\"mgnlBgLight mgnlImportExport\">"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "reloadi18n.title")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"post\" action=\"\">"); //$NON-NLS-1$

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + PARAM_RELOAD_ACTION
            + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get(request, "reloadi18n.action") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }
}
