package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Bootstrapper.VersionFilter;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
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
 * Simple servlet used to import/export data from jcr using the standard jcr import/export features.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class ImportExportServlet extends EntryServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * request parameter: repository name.
     */
    private static final String PARAM_REPOSITORY = "mgnlRepository"; //$NON-NLS-1$

    /**
     * request parameter: path.
     */
    private static final String PARAM_PATH = "mgnlPath"; //$NON-NLS-1$

    /**
     * request parameter: keep versions.
     */
    private static final String PARAM_KEEPVERSIONS = "mgnlKeepVersions"; //$NON-NLS-1$

    /**
     * request parameter: imported file.
     */
    private static final String PARAM_FILE = "mgnlFileImport"; //$NON-NLS-1$

    /**
     * request parameter: export requested.
     */
    private static final String PARAM_EXPORT_ACTION = "exportxml"; //$NON-NLS-1$

    private static final String[] repositories = new String[]{
        ContentRepository.WEBSITE,
        ContentRepository.CONFIG,
        ContentRepository.USERS,
        ContentRepository.USER_ROLES};

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ImportExportServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (isAuthorized(request, response)) {
            try {
                request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
            }
            catch (IllegalStateException e) {
                // ignore
            }

            String repository = request.getParameter(PARAM_REPOSITORY);
            if (StringUtils.isEmpty(repository)) {
                repository = ContentRepository.WEBSITE;
            }
            String basepath = request.getParameter(PARAM_PATH);
            if (StringUtils.isEmpty(basepath)) {
                basepath = "/"; //$NON-NLS-1$
            }

            boolean keepVersionHistory = BooleanUtils.toBoolean(request.getParameter(PARAM_KEEPVERSIONS));

            if (request.getParameter(PARAM_EXPORT_ACTION) != null) {
                executeExport(response, repository, basepath, keepVersionHistory);
                return;
            }

            displayExportForm(request, response.getWriter(), repository, basepath);
        }
    }

    /**
     * Display a simple form for importing/exporting data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     */
    private void displayExportForm(HttpServletRequest request, PrintWriter out, String repository, String basepath) {

        out.println("<html><head><title>Magnolia</title></head><body>"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.export")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"get\" action=\"\">"); //$NON-NLS-1$

        writeRepositoryField(request, out, repository);
        writeBasePathField(request, out, basepath);
        writeKeepVersionField(request, out);

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + PARAM_EXPORT_ACTION + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get(request, "importexport.export") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    /**
     * Display a simple form for importing/exporting data.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository (extracted from request parameter or default)
     */
    private void displayImportForm(HttpServletRequest request, PrintWriter out, String repository, String basepath) {

        out.println("<html><head><title>Magnolia</title></head><body>"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get(request, "importexport.import")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"post\" action=\"\" enctype=\"multipart/form-data\">"); //$NON-NLS-1$

        writeRepositoryField(request, out, repository);
        writeBasePathField(request, out, basepath);
        writeKeepVersionField(request, out);
        out.println(MessagesManager.get(request, "importexport.file") //$NON-NLS-1$
            + " <input type=\"file\" name=\"" + PARAM_FILE + "\" /><br/>");  //$NON-NLS-1$//$NON-NLS-2$

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
            + PARAM_EXPORT_ACTION + "\" value=\"" //$NON-NLS-1$
            + MessagesManager.get(request, "importexport.import") //$NON-NLS-1$
            + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param basepath
     */
    private void writeBasePathField(HttpServletRequest request, PrintWriter out, String basepath) {
        out.println(MessagesManager.get(request, "importexport.basepath") //$NON-NLS-1$
            + " <input name=\"" //$NON-NLS-1$
            + PARAM_PATH + "\" value=\"" //$NON-NLS-1$
            + basepath + "\" /><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     */
    private void writeKeepVersionField(HttpServletRequest request, PrintWriter out) {
        out.println(MessagesManager.get(request, "importexport.keepversions") //$NON-NLS-1$
            + " <input name=\"" //$NON-NLS-1$
            + PARAM_KEEPVERSIONS + "\" value=\"true\" type=\"checkbox\"/><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param repository
     */
    private void writeRepositoryField(HttpServletRequest request, PrintWriter out, String repository) {
        out.println(MessagesManager.get(request, "importexport.repository") //$NON-NLS-1$
            + " <select name=\"" //$NON-NLS-1$
            + PARAM_REPOSITORY + "\">"); //$NON-NLS-1$
        for (int j = 0; j < repositories.length; j++) {
            out.print("<option"); //$NON-NLS-1$
            if (repository.equals(repositories[j])) {
                out.print(" selected=\"selected\""); //$NON-NLS-1$
            }
            out.print(">"); //$NON-NLS-1$
            out.print(repositories[j]);
            out.print("</option>"); //$NON-NLS-1$
        }
        out.println("</select>"); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$
    }

    /**
     * A post request is usually an import request.
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAuthorized(request, response)) {
            log.debug("Import request received."); //$NON-NLS-1$

            MultipartForm form = Resource.getPostedForm(request);
            if (form == null) {
                log.error("Missing form."); //$NON-NLS-1$
                return;
            }

            String basepath = form.getParameter(PARAM_PATH);
            if (StringUtils.isEmpty(basepath)) {
                basepath = "/"; //$NON-NLS-1$
            }

            boolean keepVersionHistory = BooleanUtils.toBoolean(form.getParameter(PARAM_KEEPVERSIONS));

            String repository = form.getParameter(PARAM_REPOSITORY);
            Document xmlFile = form.getDocument(PARAM_FILE);
            if (StringUtils.isEmpty(repository) || xmlFile == null) {
                throw new RuntimeException("Wrong parameters"); //$NON-NLS-1$
            }

            executeImport(basepath, repository, xmlFile, keepVersionHistory);

            doGet(request, response);
        }
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
        response.setContentType("text/xml"); //$NON-NLS-1$
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        String pathName = StringUtils.replace(basepath, "/", "."); //$NON-NLS-1$ //$NON-NLS-2$
        if (".".equals(pathName)) { //$NON-NLS-1$
            // root node
            pathName = StringUtils.EMPTY;
        }
        response.setHeader("content-disposition", "attachment; filename=" + repository + pathName + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                // use exportSystemView in order to preserve property types
                // http://issues.apache.org/jira/browse/JCR-115
                session.exportSystemView(basepath, stream, false, false);
            }
            else {

                // write to a temp file and then re-read it to remove version history
                File tempFile = File.createTempFile("export", "xml"); //$NON-NLS-1$ //$NON-NLS-2$
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

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("About to import file into the [{0}] repository", new Object[]{repository})); //$NON-NLS-1$
        }

        InputStream stream = xmlFile.getStream();
        Session session = ws.getSession();
        try {
            if (keepVersionHistory) {
                session.importXML(basepath, stream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            }
            else {

                // create a temporary file and save the trimmed xml
                File strippedFile = File.createTempFile("import", "xml"); //$NON-NLS-1$ //$NON-NLS-2$
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
            log.error(MessageFormat.format(
                "Unable to save changes to the [{0}] repository due to a {1} Exception: {2}.", //$NON-NLS-1$
                new Object[]{repository, e.getClass().getName(), e.getMessage()}), e);
        }

        log.info("Import done"); //$NON-NLS-1$
    }
}