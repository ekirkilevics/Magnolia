package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.Server;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Base servlet which can be subclassed if you need to return html pages. Allow access only if server is admin, handle
 * unchecked errors and set appropriate encoding.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public abstract class BasePageServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!Server.isAdmin()) {
            response.sendRedirect("/");
        }

        response.setContentType("text/html; charset=UTF-8");

        try {
            draw(request, response);
        }
        catch (Throwable e) {
            e.printStackTrace(response.getWriter());
        }

    }

    /**
     * @see javax.servlet.http.HttpServlet#doOptions(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        doGet(request, response);
    }

    protected abstract void draw(HttpServletRequest request, HttpServletResponse response) throws IOException,
        RepositoryException;

}
