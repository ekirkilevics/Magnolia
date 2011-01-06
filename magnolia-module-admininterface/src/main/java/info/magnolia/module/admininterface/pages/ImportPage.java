/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.importexport.DataTransporter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;


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
