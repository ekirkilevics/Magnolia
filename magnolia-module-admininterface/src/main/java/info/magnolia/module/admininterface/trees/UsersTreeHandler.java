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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Handles the tree rendering for the "users" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class UsersTreeHandler extends AdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public UsersTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new UsersTreeConfiguration());
    }

}