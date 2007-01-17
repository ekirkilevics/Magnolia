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

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Tree;

import javax.servlet.http.HttpServletRequest;

/**
 * A TreeConfiguration that allows more node types.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JcrBrowserTreeConfiguration extends ConfigTreeConfiguration {
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        super.prepareTree(tree, browseMode, request);
        tree.addItemType(ItemType.NT_UNSTRUCTURED);
        tree.addItemType(ItemType.EXPRESSION.getSystemName());
        tree.addItemType(ItemType.WORKITEM.getSystemName());
        tree.addItemType(ItemType.USER.getSystemName());
        tree.addItemType(ItemType.GROUP.getSystemName());
        tree.addItemType(ItemType.ROLE.getSystemName());
        tree.addItemType(ItemType.SYSTEM.getSystemName());
    }
}
