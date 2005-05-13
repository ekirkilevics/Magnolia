package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


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
     * request parameter: repository name.
     */
    private static final String PARAM_REPOSITORY = "mgnlRepository";

    /**
     * request parameter: path.
     */
    private static final String PARAM_PATH = "mgnlPath";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ImportExportServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String repository = request.getParameter(PARAM_REPOSITORY);
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }
        String basepath = request.getParameter(PARAM_PATH);
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

            Session session = ws.getSession();

            try {
                // use exportSystemView in order to preserve property types
                // http://issues.apache.org/jira/browse/JCR-115
                session.exportSystemView(basepath, stream, false, false);
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

        out.println("<h2>Export</h2>");
        out.println("<form method=\"get\" action=\"\">");
        out.println("repository: <select name=\"" + PARAM_REPOSITORY + "\"><br/>");

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

        out.println("<h2>Import</h2>");
        out.println("<form method=\"post\" action=\"\" enctype=\"multipart/form-data\">");
        out.println("repository: <select name=\"" + PARAM_REPOSITORY + "\"><br/>");

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
        out.println("file: <input type=\"file\" name=\"file\" /><br/>");
        out.println("<input type=\"submit\" name=\"importxml\" value=\"import\" />");
        out.println("</form>");

        out.println("</body></html>");

    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {

        log.debug("Import request received.");

        MultipartForm form = Resource.getPostedForm(request);
        if (form == null) {
            log.error("Missing form.");
            return;
        }

        String repository = form.getParameter(PARAM_REPOSITORY);
        Document xmlFile = form.getDocument("file");
        if (StringUtils.isEmpty(repository) || xmlFile == null) {
            throw new RuntimeException("Wrong parameters");
        }

        HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();

        log.info("About to import file into the [" + repository + "] repository");
        InputStream stream = xmlFile.getStream();
        Session session = ws.getSession();
        try {
            session.importXML("/", stream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }
        try {
            stream.close();
        }
        catch (IOException e) {
            // ignore
        }

        try {
            session.save();
        }
        catch (RepositoryException e) {
            log.error("Unable to save changes to the ["
                + repository
                + "] repository due to a "
                + e.getClass().getName()
                + " Exception: "
                + e.getMessage()
                + ".", e);
        }

        log.info("Import done");

        doGet(request, response);

    }

}