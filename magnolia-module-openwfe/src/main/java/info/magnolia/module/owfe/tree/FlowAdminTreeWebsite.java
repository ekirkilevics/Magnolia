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
package info.magnolia.module.owfe.tree;

import info.magnolia.module.admininterface.trees.WebsiteTreeConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles the tree rendering for the "website" repository.
 *
 * @author Fabrizio Giustina
 * @version $Id: WebsiteTreeHandler.java 1610 2006-01-30 23:54:23Z svk $
 */
public class FlowAdminTreeWebsite extends FlowAdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public FlowAdminTreeWebsite(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new WebsiteTreeConfiguration());
    }


 }
