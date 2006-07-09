/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface;

import info.magnolia.cms.gui.control.Tree;

import javax.servlet.http.HttpServletRequest;


/**
 * Used by the tree handler to configure the tree
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public interface AdminTreeConfiguration {

    /**
     * Override this method to configure the tree control (define the columns, ...)
     * @param tree
     * @param browseMode true if this tree is shown in simple browse mode
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request);

    /**
     * Prepare the context menu of the tree. This is called during renderTree
     * @param tree
     * @param browseMode true if this tree is shown in simple browse mode
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request);

    /**
     * Prepare the functionbar (footerbar) of the tree. This is called during renderTree
     * @param tree
     * @param browseMode true if this tree is shown in simple browse mode
     */
    public void prepareFunctionBar(Tree tree, boolean browseMode, HttpServletRequest request);

}
