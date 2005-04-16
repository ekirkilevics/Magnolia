package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;


/**
 * Simple servlet used to import/export data from jcr.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class ImportExportServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String repository = request.getParameter("repository");
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }
        String basepath = request.getParameter("basepath");
        if (StringUtils.isEmpty(basepath)) {
            basepath = "/";
        }

        if (request.getParameter("exportxml") != null) {

            HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
            Workspace ws = hr.getWorkspace();
            OutputStream stream = response.getOutputStream();
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            String pathName = StringUtils.replace(basepath, "/", ".");
            if (".".equals(pathName)) {
                // root node
                pathName = StringUtils.EMPTY;
            }
            response.setHeader("content-disposition", "attachment; filename=" + repository + pathName + ".xml");

            try {
                ws.getSession().exportDocumentView(basepath, stream, false, false);
            }
            catch (Exception e) {
                throw new NestableRuntimeException(e);
            }
            stream.flush();
            stream.close();
            return;
        }

        String[] repositories = new String[]{
            ContentRepository.WEBSITE,
            ContentRepository.CONFIG,
            ContentRepository.USERS,
            ContentRepository.USER_ROLES};

        // not exporting
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Magnolia Export servlet</title>");
        out.println("</head><body>");

        out.println("<form method=\"get\" action=\"\">");
        out.println("repository: <select name=\"repository\"><br/>");

        for (int j = 0; j < repositories.length; j++) {
            out.print("<option");
            if (repository.equals(repositories[j])) {
                out.print(" selected=\"selected\"");
            }
            out.print(">");
            out.print(repositories[j]);
            out.print("</option>");
        }

        out.println("</select>");
        out.println("<br/>");
        out.println("base path: <input name=\"basepath\" value=\"" + basepath + "\" /><br/>");
        out.println("<input type=\"submit\" name=\"exportxml\" value=\"export\" />");

        out.println("</form>");

        out.println("</body></html>");

    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {

        // @todo handle import?
        super.doPost(request, response);
    }

}