package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.MockCacheRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

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

    private CacheManager cacheManager = CacheManagerFactory.getCacheManager();

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
                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                cachePage(hm.getRoot(), request);
                cachePage(hm.getRoot(), request);
                response.getWriter().write(MessagesManager.get("cacheservlet.success"));
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
        out.println(MessagesManager.get("cacheservlet.title")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        if (Server.isAdmin())
            out.println("<h3 style=\"color:red;\">" + MessagesManager.get("cacheservlet.warning") + "</h3>");

        out.println("<form method=\"get\" action=\"\">"); //$NON-NLS-1$
        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + CACHE_GENERATE_ACTION
            + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get("cacheservlet.generate") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    private void cachePage(Content page, HttpServletRequest request) {
        Collection children = page.getChildren();
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            Content item = (Content) iter.next();
            MockCacheRequest mock = new MockCacheRequest(request, item);
            this.cacheManager.cacheRequest(mock);
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
