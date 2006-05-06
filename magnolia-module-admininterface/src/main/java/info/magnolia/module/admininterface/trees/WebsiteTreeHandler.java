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
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.jcr.version.Version;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the tree rendering for the "website" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class WebsiteTreeHandler extends AdminTreeMVCHandler {
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(WebsiteTreeHandler.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public WebsiteTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new WebsiteTreeConfiguration());
    }

}