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
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple servlet used to import/export data from jcr using the standard jcr import/export features.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ExportPage extends TemplatedMVCHandler {

    /**
     * Stable serialVersionUID.
     */
    public static final long serialVersionUID = 222L;

    public static final String MIME_TEXT_XML = "text/xml";

    public static final String MIME_GZIP = "application/x-gzip";

    public static final String MIME_APPLICATION_ZIP = "application/zip";

    /**
     * View value for the export file stream (won't render anything)
     */
    public static final String VIEW_EXPORT="export";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ExportPage.class);

    protected String mgnlRepository;

    protected String mgnlPath;

    protected boolean mgnlKeepVersions;

    private boolean mgnlFormat;

    private String ext;

    private boolean exportxml;

    /**
     * Getter for <code>ext</code>.
     * @return Returns the ext.
     */
    public String getExt() {
        return this.ext;
    }

    /**
     * Setter for <code>ext</code>.
     * @param ext The ext to set.
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * Getter for <code>mgnlFormat</code>.
     * @return Returns the mgnlFormat.
     */
    public boolean isMgnlFormat() {
        return this.mgnlFormat;
    }

    /**
     * Setter for <code>mgnlFormat</code>.
     * @param mgnlFormat The mgnlFormat to set.
     */
    public void setMgnlFormat(boolean mgnlFormat) {
        this.mgnlFormat = mgnlFormat;
    }

    /**
     * Getter for <code>mgnlKeepVersions</code>.
     * @return Returns the mgnlKeepVersions.
     */
    public boolean isMgnlKeepVersions() {
        return this.mgnlKeepVersions;
    }

    /**
     * Setter for <code>mgnlKeepVersions</code>.
     * @param mgnlKeepVersions The mgnlKeepVersions to set.
     */
    public void setMgnlKeepVersions(boolean mgnlKeepVersions) {
        this.mgnlKeepVersions = mgnlKeepVersions;
    }

    /**
     * Getter for <code>mgnlPath</code>.
     * @return Returns the mgnlPath.
     */
    public String getMgnlPath() {
        return this.mgnlPath;
    }

    /**
     * Setter for <code>mgnlPath</code>.
     * @param mgnlPath The mgnlPath to set.
     */
    public void setMgnlPath(String mgnlPath) {
        this.mgnlPath = mgnlPath;
    }

    /**
     * Getter for <code>mgnlRepository</code>.
     * @return Returns the mgnlRepository.
     */
    public String getMgnlRepository() {
        return this.mgnlRepository;
    }

    /**
     * Setter for <code>mgnlRepository</code>.
     * @param mgnlRepository The mgnlRepository to set.
     */
    public void setMgnlRepository(String mgnlRepository) {
        this.mgnlRepository = mgnlRepository;
    }

    /**
     * Getter for <code>exportxml</code>.
     * @return Returns the exportxml.
     */
    public boolean isExportxml() {
        return this.exportxml;
    }

    /**
     * Setter for <code>exportxml</code>.
     * @param exportxml The exportxml to set.
     */
    public void setExportxml(boolean exportxml) {
        this.exportxml = exportxml;
    }

    /**
     * @param name
     * @param request
     * @param response
     */
    public ExportPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandlerImpl#getCommand()
     */
    public String getCommand() {
        if (this.exportxml) {
            return "exportxml";
        }
        return super.getCommand();
    }

    /**
     * Actually perform export. The generated file is sent to the client.
     * @param response HttpServletResponse
     * @param repository selected repository
     * @param basepath base path in repository
     * @param format should we format the resulting xml
     * @param keepVersionHistory if <code>false</code> version info will be stripped from the exported document
     * @throws IOException for errors while accessing the servlet output stream
     */
    public String exportxml() throws Exception {

        if (StringUtils.isEmpty(mgnlRepository)) {
            mgnlRepository = ContentRepository.WEBSITE;
        }
        if (StringUtils.isEmpty(mgnlPath)) {
            mgnlPath = "/"; //$NON-NLS-1$
        }
        if (StringUtils.isEmpty(ext)) {
            ext = DataTransporter.XML;
        }

        if (!checkPermissions(request, mgnlRepository, mgnlPath, Permission.WRITE)) {

            throw new ServletException(new AccessDeniedException(
                "Write permission needed for export. User not allowed to WRITE path [" //$NON-NLS-1$
                    + mgnlPath
                    + "]")); //$NON-NLS-1$

        }
        HierarchyManager hr = MgnlContext.getHierarchyManager(mgnlRepository);
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

        String pathName = StringUtils.replace(mgnlPath, DataTransporter.SLASH, DataTransporter.DOT); //$NON-NLS-1$ //$NON-NLS-2$
        pathName = DataTransporter.encodePath(pathName, DataTransporter.DOT, DataTransporter.UTF8);
        if (DataTransporter.DOT.equals(pathName)) { //$NON-NLS-1$
            // root node
            pathName = StringUtils.EMPTY;
        }

        response.setHeader("content-disposition", "attachment; filename=" + mgnlRepository + pathName + ext); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream baseOutputStream = response.getOutputStream();

        try {
            DataTransporter.executeExport(
                baseOutputStream,
                mgnlKeepVersions,
                mgnlFormat,
                session,
                mgnlPath,
                mgnlRepository,
                ext);
        }
        catch (RuntimeException e) {
            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("content-disposition", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
            throw e;
        }

        return VIEW_EXPORT;
    }

    /**
     * Uses access manager to authorise this request.
     * @param request HttpServletRequest as received by the service method
     * @return boolean true if read access is granted
     */
    protected boolean checkPermissions(HttpServletRequest request, String repository, String basePath,
        long permissionType) {

        AccessManager accessManager = MgnlContext.getAccessManager(repository);
        if (accessManager != null) {
            if (!accessManager.isGranted(basePath, permissionType)) {
                return false;
            }
        }
        return true;
    }

    public void renderHtml(String view) throws IOException {
        // if we are exporing the file, everything is already done --> do not render
        if(VIEW_EXPORT.equals(view)){
            return;
        }
        super.renderHtml(view);
    }

    public Messages getMessages() {
        return MessagesManager.getMessages();
    }

    public Iterator getRepositories() {
        return ContentRepository.getAllRepositoryNames();
    }

}
