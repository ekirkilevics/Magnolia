package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Bootstrapper.VersionFilter;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Simple servlet used to import/export data from jcr, actually using the standard jcr import/export features (support
 * for the magnolia proprietary export format will be added soon).
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
     * request parameter: keep versions.
     */
    private static final String PARAM_KEEPVERSIONS = "mgnlKeepVersions";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ImportExportServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String repository = request.getParameter(PARAM_REPOSITORY);
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }
        String basepath = request.getParameter(PARAM_PATH);
        if (StringUtils.isEmpty(basepath)) {
            basepath = "/";
        }

        boolean keepVersionHistory = BooleanUtils.toBoolean(request.getParameter(PARAM_KEEPVERSIONS));

        if (request.getParameter("exportxml") != null) {
            executeExport(response, repository, basepath, keepVersionHistory);
            return;
        }

        displayForm(response, repository, basepath);
    }

    /**
     * Display a simple form for importing/exporting data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     * @throws IOException for errors while accessing the servlet output stream
     */
    private void displayForm(HttpServletResponse response, String repository, String basepath) throws IOException {
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
        out.println("repository: <select name=\"" + PARAM_REPOSITORY + "\">");

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
        out.println("base path: <input name=\"" + PARAM_PATH + "\" value=\"" + basepath + "\" /><br/>");
        out
            .println("keep versions: <input name=\""
                + PARAM_KEEPVERSIONS
                + "\" value=\"true\" type=\"checkbox\"/><br/>");
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
        out.println("base path: <input name=\"" + PARAM_PATH + "\" value=\"" + basepath + "\" /><br/>");
        out
            .println("keep versions: <input name=\""
                + PARAM_KEEPVERSIONS
                + "\" value=\"true\" type=\"checkbox\"/><br/>");
        out.println("file: <input type=\"file\" name=\"file\" /><br/>");
        out.println("<input type=\"submit\" name=\"importxml\" value=\"import\" />");
        out.println("</form>");

        out.println("</body></html>");
    }

    /**
     * A post request is usually an import request.
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

        String basepath = form.getParameter(PARAM_PATH);
        if (StringUtils.isEmpty(basepath)) {
            basepath = "/";
        }

        boolean keepVersionHistory = BooleanUtils.toBoolean(form.getParameter(PARAM_KEEPVERSIONS));

        String repository = form.getParameter(PARAM_REPOSITORY);
        Document xmlFile = form.getDocument("file");
        if (StringUtils.isEmpty(repository) || xmlFile == null) {
            throw new RuntimeException("Wrong parameters");
        }

        executeImport(basepath, repository, xmlFile, keepVersionHistory);

        doGet(request, response);

    }

    /**
     * Actually perform export. The generated file is sent to the client.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository
     * @param keepVersionHistory if <code>false</code> version info will be stripped from the exported document
     * @throws IOException for errors while accessing the servlet output stream
     */
    private void executeExport(HttpServletResponse response, String repository, String basepath,
        boolean keepVersionHistory) throws IOException {
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
            if (keepVersionHistory) {
                // use exportSystemView in order to preserve property types
                // http://issues.apache.org/jira/browse/JCR-115
                session.exportSystemView(basepath, stream, false, false);
            }
            else {

                // write to a temp file and then re-read it to remove version history
                File tempFile = File.createTempFile("export", "xml");
                tempFile.deleteOnExit();
                OutputStream fileStream = new FileOutputStream(tempFile);

                session.exportSystemView(basepath, fileStream, false, false);

                try {
                    fileStream.close();
                }
                catch (IOException e) {
                    // ignore
                }

                InputStream fileInputStream = new FileInputStream(tempFile);

                // use XMLSerializer and a SAXFilter in order to rewrite the file
                XMLReader reader = new VersionFilter(XMLReaderFactory
                    .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));

                reader.setContentHandler(new XMLSerializer(stream, new OutputFormat()));

                reader.parse(new InputSource(fileInputStream));

                try {
                    fileInputStream.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }

        stream.flush();
        stream.close();
        return;
    }

    /**
     * Perform import.
     * @param repository selected repository
     * @param basepath base path in repository
     * @param xmlFile uploaded file
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     */
    private void executeImport(String basepath, String repository, Document xmlFile, boolean keepVersionHistory) {
        HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();

        log.info("About to import file into the [" + repository + "] repository");
        InputStream stream = xmlFile.getStream();
        Session session = ws.getSession();
        try {
            if (keepVersionHistory) {
                session.importXML(basepath, stream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            }
            else {

                // create a temporary file and save the trimmed xml
                File strippedFile = File.createTempFile("import", "xml");
                strippedFile.deleteOnExit();

                FileOutputStream outstream = new FileOutputStream(strippedFile);

                // use XMLSerializer and a SAXFilter in order to rewrite the file
                XMLReader reader = new VersionFilter(XMLReaderFactory
                    .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
                reader.setContentHandler(new XMLSerializer(outstream, new OutputFormat()));

                try {
                    reader.parse(new InputSource(stream));
                }
                finally {
                    stream.close();
                }

                // return the filtered file as an input stream
                InputStream filteredStream = new FileInputStream(strippedFile);
                try {
                    session.importXML(
                        basepath,
                        filteredStream,
                        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
                }
                finally {
                    try {
                        filteredStream.close();
                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
            }
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
    }
}