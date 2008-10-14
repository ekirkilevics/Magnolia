/**
 * This file Copyright (c) 2007-2008 Magnolia International
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
package info.magnolia.setup.for3_6;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Checks nodes for occurrence of mix:versionable supertype.
 * @author had
 * @version $Id: $
 *
 */
public class CheckNodesForMixVersionable extends AbstractTask {

    private static Logger log = LoggerFactory.getLogger(CheckNodesForMixVersionable.class);

    public static final String UPDATE_PATH= "server/install/3_6";
    public static final String CONTENT_CHECK_FLAG = "contentMigrated";

    public CheckNodesForMixVersionable() {
        super("Check existing top level nodes", "Checks existing top level nodes for existence of mix:versionable.");
    }

    public void execute(InstallContext installContext)
            throws TaskExecutionException {
        HierarchyManager hm = installContext.getHierarchyManager("website");
        try {
            // fast track for EE users
            HierarchyManager chm = installContext.getConfigHierarchyManager();
            if (chm.getRoot().hasContent(CheckNodesForMixVersionable.UPDATE_PATH)) {
                Content c = chm.getRoot().getContent(CheckNodesForMixVersionable.UPDATE_PATH);
                if (c.hasNodeData(CheckNodesForMixVersionable.CONTENT_CHECK_FLAG) && c.getNodeData(CheckNodesForMixVersionable.CONTENT_CHECK_FLAG).getBoolean()) {
                    return;
                }
            }

            Iterator iter = hm.getRoot().getChildren().iterator();
            while (iter.hasNext()) {
                NodeType[] nt = ((Content) iter.next()).getMixinNodeTypes();
                for (int i = 0; i < nt.length; i++) {
                    if ("mix:versionable".equals(nt[i].getName())) {
                        installContext.warn("There are nodes in your repository that contain unnecessary mix:versionable. Please replace all mix:versionable with mix:referenceable supertype to achieve optimal repository performance. Refer to Magnolia Documentation at http://documentation.magnolia.info/releases/3-6.html for details. An executable is provided for Enterprise Customers.");
                        return;
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            //installContext.error(e.getMessage(), e);
            throw new TaskExecutionException("Failed to execute content node super type check.", e);
        }
    }
}
