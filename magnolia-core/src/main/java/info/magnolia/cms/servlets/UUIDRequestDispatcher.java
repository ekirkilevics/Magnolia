/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @deprecated 
 *
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class UUIDRequestDispatcher extends HttpServlet {

    /**
     * Generated
     */
    private static final long serialVersionUID = 1725760548580236125L;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(UUIDRequestDispatcher.class);

    /**
     * repository name parameter
     */
    public static final String ATTRIBUTE_REPOSITRY_NAME = "mgnlRepositoryName";

    /**
     * workspace name parameter
     */
    public static final String ATTRIBUTE_WORKSPACE_NAME = "mgnlWorkspaceName";

    /**
     * uuid
     */
    public static final String ATTRIBUTE_UUID = "mgnlUUID";

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String repositoryName = request.getParameter(ATTRIBUTE_REPOSITRY_NAME);
        String workspaceName = request.getParameter(ATTRIBUTE_WORKSPACE_NAME);
        String uuid = request.getParameter(ATTRIBUTE_UUID);
        String extension = MgnlContext.getAggregationState().getExtension();
        if (StringUtils.isEmpty(repositoryName)) {
            repositoryName = ContentRepository.WEBSITE;
        }
        if (StringUtils.isEmpty(workspaceName)) {
            workspaceName = ContentRepository.getDefaultWorkspace(repositoryName);
        }
        try {
            String handle = ContentRepository
                .getHierarchyManager(repositoryName, workspaceName)
                .getContentByUUID(uuid)
                .getHandle();
            handle = (handle + "." + extension);
            RequestDispatcher dispatcher = request.getRequestDispatcher(handle);
            dispatcher.forward(request, response);
        }
        catch (Exception e) {
            log.error("Failed to retrieve content for UUID : " + uuid + " , " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Exception caught", e);
            }
        }

    }

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
