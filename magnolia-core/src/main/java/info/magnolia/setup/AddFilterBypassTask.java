/**
 * This file Copyright (c) 2007-2011 Magnolia International
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
package info.magnolia.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;


/**
 * Adds bypass to an existing filter.
 * @author vsteller
 * @version $Id$
 *
 */
public class AddFilterBypassTask extends AbstractRepositoryTask implements Task {

    private static final String BYPASSES_NODE = "bypasses";

    private final String filterPath;
    private final String bypassName;
    private final Class bypassClass;
    private final String bypassPattern;

    public AddFilterBypassTask(String filterPath, String bypassName, Class bypassClass, String bypassPattern) {
        super("Filters", "Adds a bypass with pattern '" + bypassPattern + "' to the filter " + filterPath + "");
        this.filterPath = filterPath;
        this.bypassName = bypassName;
        this.bypassClass = bypassClass;
        this.bypassPattern = bypassPattern;
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = installContext.getConfigHierarchyManager();
        final Content filter = hm.getContent(filterPath);
        final Content bypasses = ContentUtil.getOrCreateContent(filter, BYPASSES_NODE, ItemType.CONTENTNODE);

        final Content newBypass = ContentUtil.getOrCreateContent(bypasses, bypassName, ItemType.CONTENTNODE);
        newBypass.createNodeData("class", bypassClass.getName());
        newBypass.createNodeData("pattern", bypassPattern);
    }
}
