package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;


/**
 * Simple servlet used to import/export data from jcr using the standard jcr import/export features.
 *
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ImportExportServlet extends ContextSensitiveServlet {

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
     * request parameter: format
     */
    private static final String PARAM_FORMAT = "mgnlFormat"; //$NON-NLS-1$

    /**
     * request parameter: imported file.
     */
    private static final String PARAM_FILE = "mgnlFileImport"; //$NON-NLS-1$

    /**
     * request parameter: UUID behavior for import.
     */
    private static final String PARAM_UUID_BEHAVIOR = "mgnlUuidBehavior"; //$NON-NLS-1$

    /**
     * request parameter: redirect page after import.
     */
    private static final String PARAM_REDIRECT = "mgnlRedirect"; //$NON-NLS-1$

    /**
     * request parameter: export requested.
     */
    private static final String PARAM_EXPORT_ACTION = "exportxml"; //$NON-NLS-1$

    private static final String PARAM_EXTENSION = "ext";
    public static final String MIME_TEXT_XML = "text/xml";
    public static final String MIME_GZIP = "application/x-gzip";
    public static final String MIME_APPLICATION_ZIP = "application/zip";

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doGet(request, response);

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
        String extension = request.getParameter(PARAM_EXTENSION);
        if (StringUtils.isEmpty(extension)) {
            extension = DataTransporter.XML;
        }


        boolean keepVersionHistory = BooleanUtils.toBoolean(request.getParameter(PARAM_KEEPVERSIONS));
        boolean format = BooleanUtils.toBoolean(request.getParameter(PARAM_FORMAT));

        if (request.getParameter(PARAM_EXPORT_ACTION) != null) {

            if (checkPermissions(request, repository, basepath, Permission.WRITE)) {
                executeExport(response, repository, basepath, format, keepVersionHistory, extension);
                return;
            }

            throw new ServletException(new AccessDeniedException(
                    "Write permission needed for export. User not allowed to WRITE path [" //$NON-NLS-1$
                            + basepath
                            + "]")); //$NON-NLS-1$
        }

        if (StringUtils.contains(request.getRequestURI(), "import")) { //$NON-NLS-1$
            displayImportForm(response.getWriter(), repository, basepath);
        } else {
            displayExportForm(response.getWriter(), repository, basepath);
        }
    }

    /**
     * Display a simple form for importing/exporting data.
     *
     * @param repository selected repository
     * @param basepath   base path in repository (extracted from request parameter or default)
     */
    private void displayExportForm(PrintWriter out, String repository, String basepath) {

        out.println("<html><head><title>Magnolia</title>"); //$NON-NLS-1$
        // @todo FIXME! out.println(new
        // Sources(request.getContextPath()).getHtmlCss());
        out.println("</head><body class=\"mgnlBgLight mgnlImportExport\">"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get("importexport.export")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"get\" action=\"\">"); //$NON-NLS-1$

        writeRepositoryField(out, repository);
        writeBasePathField(out, basepath);
        writeKeepVersionField(out);
        writeFormatField(out);
        writeExtensionField(out);

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
                + PARAM_EXPORT_ACTION
                + "\" value=\"" //$NON-NLS-1$
                + MessagesManager.get("importexport.export") //$NON-NLS-1$
                + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    /**
     * Display a simple form for importing/exporting data.
     *
     * @param repository selected repository
     * @param basepath   base path in repository (extracted from request parameter or default)
     */
    private void displayImportForm(PrintWriter out, String repository, String basepath) {

        out.println("<html><head><title>Magnolia</title>"); //$NON-NLS-1$
        // @todo FIXME! out.println(new
        // Sources(request.getContextPath()).getHtmlCss());
        out.println("</head><body class=\"mgnlBgLight mgnlImportExport\">"); //$NON-NLS-1$

        out.println("<h2>"); //$NON-NLS-1$
        out.println(MessagesManager.get("importexport.import")); //$NON-NLS-1$
        out.println("</h2>"); //$NON-NLS-1$
        out.println("<form method=\"post\" action=\"\" enctype=\"multipart/form-data\">"); //$NON-NLS-1$

        writeRepositoryField(out, repository);
        writeBasePathField(out, basepath);
        writeKeepVersionField(out);
        out.println(MessagesManager.get("importexport.file") //$NON-NLS-1$
                + " <input type=\"file\" name=\"" + PARAM_FILE + "\" /><br/>"); //$NON-NLS-1$//$NON-NLS-2$

        out.println("<input type=\"radio\" name=\"" //$NON-NLS-1$
                + PARAM_UUID_BEHAVIOR
                + "\" value=\"" //$NON-NLS-1$
                + ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW
                + "\">"); //$NON-NLS-1$
        out.println(MessagesManager.get("importexport.createnew")); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$

        out.println("<input type=\"radio\" name=\"" //$NON-NLS-1$
                + PARAM_UUID_BEHAVIOR
                + "\" value=\"" //$NON-NLS-1$
                + ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING
                + "\">"); //$NON-NLS-1$
        out.println(MessagesManager.get("importexport.removeexisting")); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$

        out.println("<input type=\"radio\" name=\"" //$NON-NLS-1$
                + PARAM_UUID_BEHAVIOR
                + "\" value=\"" //$NON-NLS-1$
                + ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING
                + "\">"); //$NON-NLS-1$
        out.println(MessagesManager.get("importexport.replaceexisting")); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$

        out.println("<input type=\"submit\" name=\"" //$NON-NLS-1$
                + PARAM_EXPORT_ACTION
                + "\" value=\"" //$NON-NLS-1$
                + MessagesManager.get("importexport.import") //$NON-NLS-1$
                + "\" />"); //$NON-NLS-1$

        out.println("</form></body></html>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param basepath
     */
    private void writeBasePathField(PrintWriter out, String basepath) {
        out.println(MessagesManager.get("importexport.basepath") //$NON-NLS-1$
                + " <input name=\"" //$NON-NLS-1$
                + PARAM_PATH
                + "\" value=\"" //$NON-NLS-1$
                + basepath
                + "\" /><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     */
    private void writeKeepVersionField(PrintWriter out) {
        out.println(MessagesManager.get("importexport.keepversions") //$NON-NLS-1$
                + " <input name=\"" //$NON-NLS-1$
                + PARAM_KEEPVERSIONS
                + "\" value=\"true\" type=\"checkbox\"/><br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     */
    private void writeFormatField(PrintWriter out) {
        out.println(MessagesManager.get("importexport.format") //$NON-NLS-1$
                + " <input name=\"" //$NON-NLS-1$
                + PARAM_FORMAT
                + "\" value=\"true\" type=\"checkbox\"/><br/>"); //$NON-NLS-1$
    }

    private void writeExtensionField(PrintWriter out) {
        out.println(MessagesManager.get("importexport.extension") //$NON-NLS-1$
                + " <select name=\"" //$NON-NLS-1$
                + PARAM_EXTENSION
                + "\">"); //$NON-NLS-1$
        out.println("<option selected=\"selected\" value=\"" + DataTransporter.XML + "\">Xml</option>");
        out.println("<option value=\"" + DataTransporter.ZIP + "\">Zip</option>");
        out.println("<option value=\"" + DataTransporter.GZ + "\">Gzip</option>");
        out.println("</select>"); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param repository
     */
    private void writeRepositoryField(PrintWriter out, String repository) {
        out.println(MessagesManager.get("importexport.repository") //$NON-NLS-1$
                + " <select name=\"" //$NON-NLS-1$
                + PARAM_REPOSITORY
                + "\">"); //$NON-NLS-1$
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String name = (String) repositoryNames.next();
            out.print("<option"); //$NON-NLS-1$
            if (repository.equals(name)) {
                out.print(" selected=\"selected\""); //$NON-NLS-1$
            }
            out.print(">"); //$NON-NLS-1$
            out.print(name);
            out.print("</option>"); //$NON-NLS-1$
        }
        out.println("</select>"); //$NON-NLS-1$
        out.println("<br/>"); //$NON-NLS-1$
    }

    /**
     * A post request is usually an import request.
     *
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doPost(request, response);

        if (log.isDebugEnabled()) {
            log.debug("Import request received."); //$NON-NLS-1$
        }

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

        String uuidBehaviorString = form.getParameter(PARAM_UUID_BEHAVIOR);

        int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;
        if (NumberUtils.isNumber(uuidBehaviorString)) {
            uuidBehavior = Integer.parseInt(uuidBehaviorString);
        }

        if (checkPermissions(request, repository, basepath, Permission.WRITE)) {
            DataTransporter.executeImport(basepath, repository, xmlFile, keepVersionHistory, uuidBehavior, true);
            log.info("Import done"); //$NON-NLS-1$
        } else {
            throw new ServletException(new AccessDeniedException(
                    "Write permission needed for import. User not allowed to WRITE path [" //$NON-NLS-1$
                            + basepath
                            + "]")); //$NON-NLS-1$
        }

        String redirectPage = form.getParameter(PARAM_REDIRECT);
        if (StringUtils.isNotBlank(redirectPage)) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Redirecting to [{0}]", //$NON-NLS-1$
                        new Object[]{redirectPage}));
            }
            response.sendRedirect(redirectPage);
        } else {
            doGet(request, response);
        }
    }

    /**
     * Actually perform export. The generated file is sent to the client.
     *
     * @param response           HttpServletResponse
     * @param repository         selected repository
     * @param basepath           base path in repository
     * @param format             should we format the resulting xml
     * @param keepVersionHistory if <code>false</code> version info will be stripped from the exported document
     * @throws IOException for errors while accessing the servlet output stream
     */
    private void executeExport(HttpServletResponse response, String repository,
                               String basepath, boolean format, boolean keepVersionHistory, String ext) throws IOException {
        HierarchyManager hr = MgnlContext.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();
        Session session = ws.getSession();

        if (ext.equalsIgnoreCase(DataTransporter.ZIP)) {
            response.setContentType(MIME_APPLICATION_ZIP);
        }
        else if (ext.equalsIgnoreCase(DataTransporter.GZ)) {
            response.setContentType(MIME_GZIP);
        }
        else {
            response.setContentType(MIME_TEXT_XML); 
            response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        }

        String pathName = StringUtils.replace(basepath, "/", "."); //$NON-NLS-1$ //$NON-NLS-2$
        if (".".equals(pathName)) { //$NON-NLS-1$
            // root node
            pathName = StringUtils.EMPTY;
        }
        response.setHeader("content-disposition", "attachment; filename=" + repository + pathName + ext); //$NON-NLS-1$ //$NON-NLS-2$ 
        OutputStream baseOutputStream = response.getOutputStream();
        DataTransporter.executeExport(baseOutputStream, keepVersionHistory, format, session, basepath, repository, ext);
    }


    /**
     * Uses access manager to authorise this request.
     *
     * @param request HttpServletRequest as received by the service method
     * @return boolean true if read access is granted
     */
    protected boolean checkPermissions(HttpServletRequest request, String repository, String basePath,
                                       long permissionType) {
        if (MgnlContext.getAccessManager(repository) != null) {
            if (!MgnlContext.getAccessManager(ContentRepository.WEBSITE).isGranted(basePath, permissionType)) {
                return false;
            }
        }
        return true;
    }
}