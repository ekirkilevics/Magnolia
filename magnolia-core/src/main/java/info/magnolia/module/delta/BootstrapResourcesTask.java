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

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.module.InstallContext;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Base class for tasks which bootstrap resources.
 * 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class BootstrapResourcesTask extends AbstractTask {
    private final int importUUIDBehavior;

    public BootstrapResourcesTask(String name, String description) {
        this(name, description, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
    }
    
    public BootstrapResourcesTask(String name, String description, int importUUIDBehavior) {
        super(name, description);
        this.importUUIDBehavior = importUUIDBehavior;
    }

    // TODO : check if nodes were already there
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        try {
            final String[] resourcesToBootstrap = getResourcesToBootstrap(installContext);
            BootstrapUtil.bootstrap(resourcesToBootstrap, importUUIDBehavior);
        } catch (IOException e) {
            throw new TaskExecutionException("Could not bootstrap: " + e.getMessage(), e);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not bootstrap: " + e.getMessage(), e);
        }
    }

    /**
     * Override this method to bootstrap specific resource files.
     */
    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
        String[] resourcesToBootstrap = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {
            public boolean accept(final String name) {
                return acceptResource(installContext, name);
            }
        });
        return resourcesToBootstrap;

    }

    /**
     * Override this method to filter resources to bootstrap.
     */
    protected boolean acceptResource(final InstallContext installContext, final String resourceName) {
        return false;
    }
}
