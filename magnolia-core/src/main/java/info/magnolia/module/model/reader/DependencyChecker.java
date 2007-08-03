/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.model.reader;

import info.magnolia.cms.beans.config.MissingDependencyException;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DependencyChecker {

    /**
     * @param moduleDefinitions a Map<String, ModuleDefinition> where the key is the module name.
     */
    public void checkDependencies(Map moduleDefinitions) throws ModuleDependencyException {
        final Iterator it = moduleDefinitions.values().iterator();
        while (it.hasNext()) {
            final ModuleDefinition def = (ModuleDefinition) it.next();

            final Iterator dependenciesIter = def.getDependencies().iterator();
            while (dependenciesIter.hasNext()) {
                final DependencyDefinition dep = (DependencyDefinition) dependenciesIter.next();
                if (!dep.isOptional()) {
                    checkSpecificDependency(def, dep, moduleDefinitions);
                }
            }
        }
    }

    /**
     * @param moduleDefinitions a Map<String, ModuleDefinition> where the key is the module name.
     */
    public List sortByDependencyLevel(Map moduleDefinitions) {
        final List modules = new ArrayList(moduleDefinitions.values());

        Collections.sort(modules, new DependencyLevelComparator(moduleDefinitions));

        return modules;
    }

    protected void checkSpecificDependency(ModuleDefinition checkedModule, DependencyDefinition requiredDependency, Map moduleDefinitions) throws MissingDependencyException, ModuleDependencyException {
        final ModuleDefinition installedModule = (ModuleDefinition) moduleDefinitions.get(requiredDependency.getName());
        if (installedModule == null) {
            throw new ModuleDependencyException("Module " + checkedModule + " is dependent on " + requiredDependency + ", which was not found.");
        }

        final VersionRange requiredRange = requiredDependency.getVersionRange();
        final Version installedVersion = installedModule.getVersionDefinition();

        // TODO ignore ${project.version} ? or be smarter ?
//        if (instVersion.equals("${project.version}")) {
//            log.info("module " + requiredDependency.getName() + " has a dynamic version [" + instVersion + "]. checks ignored");
//            return;
//        }

        if (!requiredRange.contains(installedVersion)) {
            throw new ModuleDependencyException("Module " + checkedModule + " is dependent on " + requiredDependency + ", but " + installedModule + " is currently installed.");
        }
    }
}
