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
