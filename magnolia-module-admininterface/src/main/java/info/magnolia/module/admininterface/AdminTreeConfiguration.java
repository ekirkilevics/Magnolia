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
package info.magnolia.module.admininterface;

import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.i18n.Messages;

import javax.servlet.http.HttpServletRequest;


/**
 * Used by the tree handler to configure the tree
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface AdminTreeConfiguration {

    /**
     * Sets the messages bundle to be used. Must be called by instanciator.
     */
    public void setMessages(Messages m);

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
