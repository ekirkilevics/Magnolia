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
package info.magnolia.module.model.reader;

import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A comparator used to sort modules according to their dependencies.
 * It hardcodes "core" to be first and "webapp" to be last.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class DependencyLevelComparator implements Comparator<ModuleDefinition> {
    private final Map<String, ModuleDefinition> allKnownModulesDefinitions;

    DependencyLevelComparator(Map<String, ModuleDefinition> allKnownModulesDefinitions) {
        this.allKnownModulesDefinitions = allKnownModulesDefinitions;
    }

    public int compare(ModuleDefinition def1, ModuleDefinition def2) {

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

        int level1 = calcDependencyDepth(def1);
        int level2 = calcDependencyDepth(def2);

        // lower level first
        int dif = level1 - level2;
        if (dif != 0) {
            return dif;
        }

        // rest is ordered alphabetically
        return def1.getName().compareTo(def2.getName());

    }

    /**
     * Calculates the depth of dependency. 0 means no dependency. If none of the dependencies
     * has itself dependencies, the level will be 1. If one or more of the dependencies has
     * dependencies that has a dependency it would return 2. And so on...
     *
     * @param def module definition
     * @return the level
     */
    protected int calcDependencyDepth(ModuleDefinition def) {
        if (def.getDependencies() == null || def.getDependencies().size() == 0) {
            return 0;
        }
        final List<Integer> dependencyLevels = new ArrayList<Integer>();
        for (final DependencyDefinition dep : def.getDependencies()) {
            final ModuleDefinition depDef = allKnownModulesDefinitions.get(dep.getName());
            if (depDef == null && !dep.isOptional()) {
                throw new RuntimeException("Missing definition for module:" + dep.getName());
            } else if (depDef != null) {
                dependencyLevels.add(new Integer(calcDependencyLevel(depDef)));
            }
        }
        return (Collections.max(dependencyLevels)).intValue() + 1;
    }

    /**
     * @deprecated since Magnolia 4.1, renamed to calcDependencyDepth()
     */
    protected int calcDependencyLevel(ModuleDefinition def) {
        return calcDependencyDepth(def);
    }

}
