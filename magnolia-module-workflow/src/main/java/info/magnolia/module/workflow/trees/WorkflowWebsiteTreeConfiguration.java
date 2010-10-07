/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.workflow.trees;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.module.admininterface.trees.WebsiteTreeConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link info.magnolia.module.admininterface.AdminTreeConfiguration} for the website tree, which redefines the activation context menu items,
 * so that they popup the activation dialog ('enter comment'). 
 *
 * @author pbracher
 * @version $Id$
 *
 */
public class WorkflowWebsiteTreeConfiguration extends WebsiteTreeConfiguration {

    private static Logger log = LoggerFactory.getLogger(WorkflowWebsiteTreeConfiguration.class);

    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        super.prepareContextMenu(tree, browseMode, request);
        if(!browseMode){
            ContextMenuItem activate = tree.getMenu().getMenuItemByName("activate");
            activate.setOnclick("mgnl.workflow.WorkflowWebsiteTree.enterComment(" + tree.getJavascriptTree() + ", " + Tree.ACTION_ACTIVATE + ", false);");

            ContextMenuItem activateInclSubs = tree.getMenu().getMenuItemByName("activateInclSubs");
            activateInclSubs.setOnclick("mgnl.workflow.WorkflowWebsiteTree.enterComment(" + tree.getJavascriptTree() + ", " + Tree.ACTION_ACTIVATE + ", true);");
        }
    }
}
