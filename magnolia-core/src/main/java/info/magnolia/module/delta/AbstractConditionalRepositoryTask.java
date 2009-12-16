/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * An abstract implementation of a RepositoryTask that only needs to be executed when a specific node is not found in the
 * repository. Can be used to easily create self-check tasks for mandatory configuration.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractConditionalRepositoryTask extends AbstractRepositoryTask {

    /**
     * @param name task name
     * @param description task description
     */
    public AbstractConditionalRepositoryTask(String name, String description) {
        super(name, description);
    }

    /**
     * Returns a path in a repository in the form <code>repository:path</code> (e.g.
     * <code>config:server/activation</code>) that will checked. Only if such path doesn't exist, doExecute() will be
     * called.
     * @return repository:path string
     */
    public abstract String getCheckedPath();

    /**
     * {@inheritDoc}
     */
    public void execute(InstallContext ctx) throws TaskExecutionException {

        boolean executeTask = false;

        String[] tokens = StringUtils.split(getCheckedPath(), ":");
        if (tokens.length != 2) {
            log.error("Invalid checked path " + getCheckedPath() + " in " + this + ". Task will not be performed");
        }
        HierarchyManager hm = ctx.getHierarchyManager(tokens[0]);

        if (hm == null) {
            log.error("Repository "
                + tokens[0]
                + " requested in "
                + this
                + " not available. Task will not be performed");
        }
        try {
            hm.getContent(tokens[1]);
        }
        catch (PathNotFoundException e) {
            // ok, this is expected
            executeTask = true;
        }
        catch (RepositoryException e) {
            throw new TaskExecutionException("Could not execute task: " + e.getMessage(), e);
        }

        if (executeTask) {
            try {
                doExecute(ctx);
            }
            catch (RepositoryException re) {
                throw new TaskExecutionException("Could not execute task: " + re.getMessage(), re);
            }
        }
    }

}
