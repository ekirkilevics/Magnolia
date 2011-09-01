/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.objectfactory.guice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleLifecycleContextImpl;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.objectfactory.Classes;


/**
 * ModuleManager that creates a child Guice Injector in which it will load configuration from module definitions.
 *
 * @version $Id$
 */
@Singleton
public class GuiceModuleManager extends ModuleManagerImpl {

    private Map<String, ModuleInstanceProvider<?>> moduleProviders = new ConcurrentHashMap<String, ModuleInstanceProvider<?>>();

    @Inject
    public GuiceModuleManager(InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader, ModuleRegistry moduleRegistry, DependencyChecker dependencyChecker) {
        super(installContext, moduleDefinitionReader, moduleRegistry, dependencyChecker);
    }

    @Override
    protected void startModule(Object moduleInstance, ModuleDefinition moduleDefinition, ModuleLifecycleContextImpl lifecycleContext) {

        super.startModule(moduleInstance, moduleDefinition, lifecycleContext);

        // Start observation now that the module has started
        ModuleInstanceProvider<?> moduleInstanceProvider = moduleProviders.get(moduleDefinition.getName());
        if (moduleInstanceProvider != null) {
            moduleInstanceProvider.startObservation();
        }
    }

    @Override
    public void stopModule(Object moduleInstance, ModuleDefinition moduleDefinition, ModuleLifecycleContextImpl lifecycleContext) {
        super.stopModule(moduleInstance, moduleDefinition, lifecycleContext);
    }

    public Class<Object> getModuleClass(ModuleDefinition moduleDefinition) throws ClassNotFoundException {
        return Classes.getClassFactory().forName(moduleDefinition.getClassName());
    }

    public void registerModuleInstanceProvider(String name, ModuleInstanceProvider provider) {
        moduleProviders.put(name, provider);
    }

}
