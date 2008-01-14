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
package info.magnolia.module.delta;

import org.apache.commons.lang.StringUtils;

import info.magnolia.module.InstallContext;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class ModuleDependencyBootstrapTask extends IsModuleInstalledDelegateTask {

    public ModuleDependencyBootstrapTask(final String dependencyName) {
        super("Bootstrap " + dependencyName, "Bootstraps " + dependencyName + " content if installed", dependencyName, new BootstrapResourcesTask("",""){

            /**
             * Accepts any resource directly under "/mgnl-bootstrap/moduleName/dependencyName" but not recursive into sub-directories.
             */
            protected boolean acceptResource(InstallContext ctx, String name) {
                final String moduleName = ctx.getCurrentModuleDefinition().getName();
                final String resourceFilename = StringUtils.substringAfter(name, "/mgnl-bootstrap/" + moduleName + "/" + dependencyName + "/");
                return !StringUtils.contains(resourceFilename, "/") && resourceFilename.endsWith(".xml");
            }
        });
    }

}
