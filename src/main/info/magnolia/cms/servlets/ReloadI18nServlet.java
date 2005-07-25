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
     * access the MessagesManager and ask him to reload the bundles
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MessagesManager.getMessages(request, null, MessagesManager.getDefaultLocale()).reloadBundles();
        PrintWriter out = response.getWriter();
        out.println("Bundle reloaded:" + new SimpleDateFormat().format(new Date())); //$NON-NLS-1$
        out.flush();
        out.close();
    }
}
