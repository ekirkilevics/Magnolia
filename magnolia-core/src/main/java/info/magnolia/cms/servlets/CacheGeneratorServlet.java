package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.MockCacheRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Load all the nodes of a website to that they can be cached for immediate production use.
 * @author niko
 */
public class CacheGeneratorServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String CACHE_GENERATE_ACTION = "create.cache";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CacheGeneratorServlet.class);

    /**
     * access the MessagesManager and ask him to reload the bundles
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getParameter(CACHE_GENERATE_ACTION) == null) {
            displayCacheForm(request, response.getWriter());
        }
        else {
            try {
                HierarchyManager hm = SessionAccessControl.getHierarchyManager(request);
                Workspace ws = hm.getWorkspace();
                cachePage(hm.getRoot(), request);
                cachePage(hm.getRoot(), request);
                response.getWriter().write(MessagesManager.get(request, "cacheservlet.success"));

            }
            catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    /**
     * Display a simple form for importing/exporting data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     */
    private void displayCacheForm(HttpServletRequest request, PrintWriter out) {

        out.println("<html><head><title>Magnolia</title>"); //$NON-NLS-1$
        // @todo FIXME! out.println(new Sources(request.getContextPath()).getHtmlCss());
        out.println("</head><body class=\"mgnlBgLight mgnlImportExport\">"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "cacheservlet.title")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        if (Server.isAdmin())
            out.println("<h3 style=\"color:red;\">" + MessagesManager.get(request, "cacheservlet.warning") + "</h3>");

        out.println("<form method=\"get\" action=\"\">"); //$NON-NLS-1$
        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + CACHE_GENERATE_ACTION
            + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get(request, "cacheservlet.generate") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    private void cachePage(Content page, HttpServletRequest request) {
        Collection children = page.getChildren();
        String rootURL = "http://" + request.getRemoteAddr() + request.getContextPath() + "/";
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            Content item = (Content) iter.next();
            String node = item.getName();
            MockCacheRequest mock = new MockCacheRequest(request, item);
            CacheHandler.cacheURI(mock);
            if (log.isDebugEnabled())
                log.debug("Trying to cache request:" + mock.getRequestURL());
            cachePage(item, request);
        }

    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        doGet(request, response);
    }
}
