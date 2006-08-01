package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;

import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ImportPage extends ExportPage {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ImportPage.class);

    private Document mgnlFileImport;

    private String mgnlRedirect;

    private int mgnlUuidBehavior;

    /**
     * @param name
     * @param request
     * @param response
     */
    public ImportPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.module.admininterface.pages.ExportPage#getCommand()
     */
    public String getCommand() {
        if ("POST".equals(getRequest().getMethod())) {
            return "importxml"; // a post request is an import request, preserve the behaviour from the servlet
        }
        return super.getCommand();
    }

    /**
     * Getter for <code>mgnlFileImport</code>.
     * @return Returns the mgnlFileImport.
     */
    public Document getMgnlFileImport() {
        return this.mgnlFileImport;
    }

    /**
     * Setter for <code>mgnlFileImport</code>.
     * @param mgnlFileImport The mgnlFileImport to set.
     */
    public void setMgnlFileImport(Document mgnlFileImport) {
        this.mgnlFileImport = mgnlFileImport;
    }

    /**
     * Getter for <code>mgnlRedirect</code>.
     * @return Returns the mgnlRedirect.
     */
    public String getMgnlRedirect() {
        return this.mgnlRedirect;
    }

    /**
     * Setter for <code>mgnlRedirect</code>.
     * @param mgnlRedirect The mgnlRedirect to set.
     */
    public void setMgnlRedirect(String mgnlRedirect) {
        this.mgnlRedirect = mgnlRedirect;
    }

    /**
     * Getter for <code>mgnlUuidBehavior</code>.
     * @return Returns the mgnlUuidBehavior.
     */
    public int getMgnlUuidBehavior() {
        return this.mgnlUuidBehavior;
    }

    /**
     * Setter for <code>mgnlUuidBehavior</code>.
     * @param mgnlUuidBehavior The mgnlUuidBehavior to set.
     */
    public void setMgnlUuidBehavior(int mgnlUuidBehavior) {
        this.mgnlUuidBehavior = mgnlUuidBehavior;
    }

    /**
     * @throws Exception
     */
    public String importxml() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Import request received."); //$NON-NLS-1$
        }

        if (StringUtils.isEmpty(mgnlRepository)) {
            mgnlRepository = ContentRepository.WEBSITE;
        }
        if (StringUtils.isEmpty(mgnlPath)) {
            mgnlPath = "/"; //$NON-NLS-1$
        }

        if (!checkPermissions(request, mgnlRepository, mgnlPath, Permission.WRITE)) {
            throw new ServletException(new AccessDeniedException(
                "Write permission needed for import. User not allowed to WRITE path [" //$NON-NLS-1$
                    + mgnlPath
                    + "]")); //$NON-NLS-1$
        }

        DataTransporter.importDocument(
            mgnlFileImport,
            mgnlRepository,
            mgnlPath,
            mgnlKeepVersions,
            mgnlUuidBehavior,
            true,
            true);

        log.info("Import done"); //$NON-NLS-1$

        if (StringUtils.isNotBlank(mgnlRedirect)) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Redirecting to [{0}]", //$NON-NLS-1$
                    new Object[]{mgnlRedirect}));
            }
            response.sendRedirect(mgnlRedirect);
            return null;
        }
        return this.show();
    }

}