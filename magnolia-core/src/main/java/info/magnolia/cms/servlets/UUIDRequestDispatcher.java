/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Path;

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
 * @author Sameer Charles
 * @version $Revision: 1831 $ ($Author: scharles $)
 */
public class UUIDRequestDispatcher extends HttpServlet {

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
        String extension = Path.getExtension(request);
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
            log.debug("Exception caught", e);
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
