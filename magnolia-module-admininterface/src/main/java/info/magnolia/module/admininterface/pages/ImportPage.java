package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.io.File;
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

    private File mgnlFileImport;

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
     * Getter for <code>mgnlFileImport</code>.
     * @return Returns the mgnlFileImport.
     */
    public File getMgnlFileImport() {
        return this.mgnlFileImport;
    }

    /**
     * Setter for <code>mgnlFileImport</code>.
     * @param mgnlFileImport The mgnlFileImport to set.
     */
    public void setMgnlFileImport(File mgnlFileImport) {
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

        MultipartForm form = Resource.getPostedForm(request);
        if (form == null) {
            log.error("Missing form."); //$NON-NLS-1$
            return null;
        }

        if (StringUtils.isEmpty(mgnlRepository)) {
            mgnlRepository = ContentRepository.WEBSITE;
        }
        if (StringUtils.isEmpty(mgnlPath)) {
            mgnlPath = "/"; //$NON-NLS-1$
        }

        if (checkPermissions(request, mgnlRepository, mgnlPath, Permission.WRITE)) {
            DataTransporter.executeImport(
                mgnlPath,
                mgnlRepository,
                mgnlFileImport,
                mgnlKeepVersions,
                mgnlUuidBehavior,
                true,
                true);
            log.info("Import done"); //$NON-NLS-1$
        }
        else {
            throw new ServletException(new AccessDeniedException(
                "Write permission needed for import. User not allowed to WRITE path [" //$NON-NLS-1$
                    + mgnlPath
                    + "]")); //$NON-NLS-1$
        }

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