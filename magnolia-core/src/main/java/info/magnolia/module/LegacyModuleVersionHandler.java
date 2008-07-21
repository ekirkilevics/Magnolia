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
package info.magnolia.module;

import info.magnolia.cms.module.Module;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;

/**
 * A ModuleVersionHandler used for modules which don't specify one and where
 * the module class is a subclass of the deprecated {@link info.magnolia.cms.module.Module}.
 * 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class LegacyModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final String EXCEPTION_DURING_INSTALLING_MODULE = "Exception during installing module using the old model";

    public Delta getInstall(InstallContext installContext) {
        Delta delta = super.getInstall(installContext);
        final ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        delta.getTasks().add(new AbstractTask("Legacy module compatibility", "This calls the register() method of modules written against the Magnolia 3.0 API.") {

            public void execute(InstallContext installContext) throws TaskExecutionException {
                try {
                    Object moduleInstance = ClassUtil.newInstance(moduleDefinition.getClassName());
                    if (moduleInstance instanceof Module) {
                        // read the module node
                        ((Module) moduleInstance).register(
                            moduleDefinition,
                            installContext.getOrCreateCurrentModuleNode(),
                            Module.REGISTER_STATE_INSTALLATION);
                    }
                    ModuleRegistry registry = ModuleRegistry.Factory.getInstance();
                    registry.registerModuleInstance(moduleDefinition.getName(), moduleInstance);
                }
                catch (Exception e) {
                    throw new TaskExecutionException(EXCEPTION_DURING_INSTALLING_MODULE, e);
                }
            }
        });
        return delta;
    }
}
