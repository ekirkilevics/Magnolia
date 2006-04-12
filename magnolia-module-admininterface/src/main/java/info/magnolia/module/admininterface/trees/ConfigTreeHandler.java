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

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the tree rendering for the "config" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ConfigTreeHandler extends AdminTreeMVCHandler {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ConfigTreeHandler.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public ConfigTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new ConfigTreeConfiguration());
    }

    /**
     * Do not active sub CONTENTNODES automatically
     */
    public String activate() {
        boolean recursive = (request.getParameter("recursive") != null); //$NON-NLS-1$
        // do not activate nodes of type CONTENTNODE if recursive is false
        try {
            this.getTree().activateNode(this.getPathSelected(), recursive, false);
        }
        catch (Exception e) {
            log.error("can't activate", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.activate") + " " + AlertUtil.getExceptionMessage(e));
        }

        return VIEW_TREE;
    }

}