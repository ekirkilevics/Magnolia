/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.module.cache.setup;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ConditionalDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Removes obsolete cache compression list in favor of new global configuration.
 * @version $Revision: $ ($Author: $)
 */
public class DefaultCompressibleContentTypesCondition extends ConditionalDelegateTask {

    private final String checkedNodeName;

    public DefaultCompressibleContentTypesCondition(String taskName, String taskDescription, Task ifTrue, Task ifFalse, String checkedNodeName) {
        super(taskName, taskDescription, ifTrue, ifFalse);
        this.checkedNodeName = checkedNodeName;
    }

    protected boolean condition(InstallContext installContext) throws TaskExecutionException {
        try {
            List vals = new ArrayList();
            HierarchyManager hm = installContext.getConfigHierarchyManager();
            if (!hm.isExist(checkedNodeName)) {
                // compression configuration doesn't exist, just return false
                return false;
            }
            Collection compressionList = hm.getContent(checkedNodeName).getNodeDataCollection();
            for (Iterator iterator = compressionList.iterator(); iterator.hasNext();) {
                NodeData data = (NodeData) iterator.next();
                vals.add(data.getString());
            }
            return vals.remove("text/html") && vals.remove("text/css") && vals.remove("application/x-javascript") && vals.isEmpty();

        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        // the node doesn't have any children or their values can't be retried. Return false so the warning will be displayed.
        return false;
    }
}
