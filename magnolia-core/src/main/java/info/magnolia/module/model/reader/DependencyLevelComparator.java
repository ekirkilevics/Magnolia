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

import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class DependencyLevelComparator implements Comparator {
    private final Map modulesDefinitions;

    DependencyLevelComparator(Map modulesDefinitions) {
        this.modulesDefinitions = modulesDefinitions;
    }

    public int compare(Object arg1, Object arg2) {
        final ModuleDefinition def1 = (ModuleDefinition) arg1;
        final ModuleDefinition def2 = (ModuleDefinition) arg2;

        // the core module must always be installed/updated/started first
        if ("core".equals(def1.getName())) {
            return -1;
        } else if ("core".equals(def2.getName())) {
            return 1;
        }

        // the webapp module must always be installed/updated/started last
        if ("webapp".equals(def1.getName())) {
            return 1;
        } else if ("webapp".equals(def2.getName())) {
            return -1;
        }

        int level1 = calcDependencyLevel(def1);
        int level2 = calcDependencyLevel(def2);

        // lower level first
        int dif = level1 - level2;
        if (dif != 0) {
            return dif;
        }

        // rest is ordered alphabetically
        return def1.getName().compareTo(def2.getName());

    }

    /**
     * Calculates the level of dependency. 0 means no dependency. If no of the dependencies has itself dependencies is
     * this level 1. If one or more of the dependencies has a dependencies has a dependency it would return 2. And so on
     * ...
     * @param def module definition
     * @return the level
     */
    protected int calcDependencyLevel(ModuleDefinition def) {
        if (def.getDependencies() == null || def.getDependencies().size() == 0) {
            return 0;
        }
        final List dependencyLevels = new ArrayList();
        for (Iterator iter = def.getDependencies().iterator(); iter.hasNext();) {
            final DependencyDefinition dep = (DependencyDefinition) iter.next();
            final ModuleDefinition depDef = (ModuleDefinition) modulesDefinitions.get(dep.getName());
            if (depDef == null && !dep.isOptional()) {
                throw new RuntimeException("Missing definition for module:" + dep.getName());
            } else if (depDef != null) {
                dependencyLevels.add(new Integer(calcDependencyLevel(depDef)));
            }
        }
        return ((Integer) Collections.max(dependencyLevels)).intValue() + 1;
    }
}
