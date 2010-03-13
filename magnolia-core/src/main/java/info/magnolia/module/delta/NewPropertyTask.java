/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 * Creates a new property. Outputs a warning if the property already exists. 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NewPropertyTask extends PropertyValuesTask {
    private final String workspaceName;
    private final String nodePath;
    private final String propertyName;
    private final String value;

    public NewPropertyTask(String name, String description, String workspaceName, String nodePath, String propertyName, String value) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.value = value;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        try {
            final Content node = hm.getContent(nodePath);
            newProperty(ctx, node, propertyName, value);
        } catch (RepositoryException e) {
            ctx.error(format("Could not create property {0} at {1}, please create it with value {2}.", propertyName, nodePath, value), e);
        }
    }
}
