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
import info.magnolia.module.model.reader.DependencyLevelComparator;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DependencyLevelComparatorTest extends TestCase {

    public void testCalcDepencyLevelWithNonOptionalDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", "1", "fake.Module", null);
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", "1", "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", "1", "fake.Module", null);
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefB.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final Map map = new HashMap();
        map.put(modDefA.getName(), modDefA);
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyLevel(modDefA));
        assertEquals(1, reg.calcDependencyLevel(modDefB));
        assertEquals(2, reg.calcDependencyLevel(modDefC));
    }

    public void testCalcDepencyLevelIgnoresUnregisteredOptionalDependencies() {
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", "1", "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", "1", "fake.Module", null);
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        depOnA.setOptional(true);
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefC.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        // mod-a is not registered in this case
        final Map map = new HashMap();
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyLevel(modDefB));
        assertEquals(1, reg.calcDependencyLevel(modDefC));
    }

    public void testCalcDepencyLevelDoesNotIgnoreRegisteredOptionalDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", "1", "fake.Module", null);
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", "1", "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", "1", "fake.Module", null);
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        depOnA.setOptional(true);
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefB.addDependency(depOnA);
        modDefC.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final Map map = new HashMap();
        map.put(modDefA.getName(), modDefA);
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyLevel(modDefA));
        assertEquals(1, reg.calcDependencyLevel(modDefB));
        assertEquals(2, reg.calcDependencyLevel(modDefC));
    }
}
