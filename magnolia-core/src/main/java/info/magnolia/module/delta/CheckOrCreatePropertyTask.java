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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CheckOrCreatePropertyTask extends PropertyValuesTask {
    private final String workspaceName;
    private final String nodePath;
    private final String propertyName;
    private final String expectedValue;

    public CheckOrCreatePropertyTask(String name, String description, String workspaceName, String nodePath, String propertyName, String expectedValue) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.expectedValue = expectedValue;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        try {
            final Content node = hm.getContent(nodePath);
            checkOrCreateProperty(ctx, node, propertyName, expectedValue);
        } catch (RepositoryException e) {
            ctx.error(format("Could not check property {0} of node at {1} which was supposed to have value {2}, please create it.", propertyName, nodePath, expectedValue), e);
        }
    }

}
