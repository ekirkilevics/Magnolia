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
package info.magnolia.module.model.reader;

import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class to check dependencies between instances of ModuleDescriptors and sort them according to
 * their dependencies.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DependencyCheckerImpl implements DependencyChecker {

    /**
     * Checks dependencies between the given modules, throws a ModuleDependencyException if there is
     * a dependency mismatch.
     *
     * @param moduleDefinitions a Map<String, ModuleDefinition> where the key is the module name.
     */
    public void checkDependencies(Map<String, ModuleDefinition> moduleDefinitions) throws ModuleDependencyException {
        for (ModuleDefinition def : moduleDefinitions.values()) {
            for (DependencyDefinition dep : def.getDependencies()) {
                if (!dep.isOptional()) {
                    checkSpecificDependency(def, dep, moduleDefinitions);
                }
            }
        }
    }

    /**
     * @param moduleDefinitions a Map<String, ModuleDefinition> where the key is the module name.
     */
    public List<ModuleDefinition> sortByDependencyLevel(Map<String, ModuleDefinition> moduleDefinitions) {
        final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(moduleDefinitions.values());

        Collections.sort(modules, new DependencyLevelComparator(moduleDefinitions));

        return modules;
    }

    protected void checkSpecificDependency(ModuleDefinition checkedModule, DependencyDefinition requiredDependency, Map<String, ModuleDefinition> moduleDefinitions) throws ModuleDependencyException {
        final ModuleDefinition dependencyModuleDef = moduleDefinitions.get(requiredDependency.getName());
        if (dependencyModuleDef == null) {
            throw new ModuleDependencyException("Module " + checkedModule + " is dependent on " + requiredDependency + ", which was not found.");
        }

        final VersionRange requiredRange = requiredDependency.getVersionRange();
        final Version dependencyVersion = dependencyModuleDef.getVersion();

        // TODO ignore ${project.version} ? or be smarter ?
//        if (instVersion.equals("${project.version}")) {
//            log.info("module " + requiredDependency.getName() + " has a dynamic version [" + instVersion + "]. checks ignored");
//            return;
//        }

        if (!requiredRange.contains(dependencyVersion)) {
            throw new ModuleDependencyException("Module " + checkedModule + " is dependent on " + requiredDependency + ", but " + dependencyModuleDef + " is currently installed.");
        }
    }
}
