/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DependencyCheckerImplTest extends TestCase {
    private DependencyChecker depChecker;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        depChecker = new DependencyCheckerImpl();
    }

    public void testSimpleDependenciesAreResolvedAndChecked() throws Exception {
        final Map modules = buildModulesMapWithDependencyOn("3.0");
        depChecker.checkDependencies(modules);
        // assert(no dependency failures)
    }


    public void testDependenciesCanUseLowerBoundInfiniteRanges() throws Exception {
        final Map modules = buildModulesMapWithDependencyOn("*/4.0");
        depChecker.checkDependencies(modules);
        // assert(no dependency failures)
    }

    public void testDependenciesCanUseUpperBoundInfiniteRanges() throws Exception {
        final Map modules = buildModulesMapWithDependencyOn("1.0/*");
        depChecker.checkDependencies(modules);
        // assert(no dependency failures)
    }

    public void testDependenciesCanUseFiniteRanges() throws Exception {
        final Map modules = buildModulesMapWithDependencyOn("1.0/4.0");
        depChecker.checkDependencies(modules);
        // assert(no dependency failures)
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfUpperBound() {
        final Map modules = buildModulesMapWithDependencyOn("1.0/2.0");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 (version 1.2.0) is dependent on module1 version 1.0/2.0, but module1 (version 3.0.0) is currently installed.", e.getMessage());
        }
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfLowerBound() {
        final Map modules = buildModulesMapWithDependencyOn("4.0/5.0");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 (version 1.2.0) is dependent on module1 version 4.0/5.0, but module1 (version 3.0.0) is currently installed.", e.getMessage());
        }
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfUpperBoundWithInfiniteLowerBound() {
        final Map modules = buildModulesMapWithDependencyOn("*/2.0");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 (version 1.2.0) is dependent on module1 version */2.0, but module1 (version 3.0.0) is currently installed.", e.getMessage());
        }
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfLowerBoundWithInfiniteUpperBound() {
        final Map modules = buildModulesMapWithDependencyOn("4.0/*");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 (version 1.2.0) is dependent on module1 version 4.0/*, but module1 (version 3.0.0) is currently installed.", e.getMessage());
        }
    }

    public void testShouldFailWhenDependencyNotFound() {
        final Map modules = buildModulesMapWithDependencyOn("Q.W");
        modules.remove("module1");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 (version 1.2.0) is dependent on module1 version Q.W, which was not found.", e.getMessage());
        }
    }

    public void testOptionalDependenciesAreIndeedOptional() throws ModuleDependencyException {
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", Version.parseVersion("1"), "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", Version.parseVersion("1"), "fake.Module", null);
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
        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);
        depChecker.checkDependencies(map);
        final List<ModuleDefinition> sorted = depChecker.sortByDependencyLevel(map);
        assertEquals(2, sorted.size());
        assertEquals(modDefB, sorted.get(0));
        assertEquals(modDefC, sorted.get(1));
    }

    public void testOptionalDependenciesStillHaveToMatchVersionRanges() throws ModuleDependencyException {
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", Version.parseVersion("1.5"), "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", Version.parseVersion("1"), "fake.Module", null);
         final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1.0/1.2");
        depOnB.setOptional(true);
        modDefC.addDependency(depOnB);
        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);
        try {
            depChecker.checkDependencies(map);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module mod-c (version 1.0.0) is dependent on mod-b version 1.0/1.2, but mod-b (version 1.5.0) is currently installed.", e.getMessage());
        }
    }

    public void testModulesShouldBeSortedAccordingToDependencies() {
        final Map modules = new HashMap();
        final ModuleDefinition module1 = new ModuleDefinition("module1", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module2 = new ModuleDefinition("module2", Version.parseVersion("3.0"), null, null);
        module2.addDependency(new DependencyDefinition("module1", "3.0", false));
        final ModuleDefinition module3 = new ModuleDefinition("module3", Version.parseVersion("3.0"), null, null);
        module3.addDependency(new DependencyDefinition("module1", "3.0", false));
        module3.addDependency(new DependencyDefinition("module2", "3.0", false));
        final ModuleDefinition module4 = new ModuleDefinition("module4", Version.parseVersion("3.0"), null, null);
        module4.addDependency(new DependencyDefinition("module1", "3.0", false));
        module4.addDependency(new DependencyDefinition("module3", "3.0", false));
        final ModuleDefinition module5 = new ModuleDefinition("module5", Version.parseVersion("3.0"), null, null);
        module5.addDependency(new DependencyDefinition("module2", "3.0", false));
        modules.put("module5", module5);
        modules.put("module4", module4);
        modules.put("module3", module3);
        modules.put("module2", module2);
        modules.put("module1", module1);

        final List list = depChecker.sortByDependencyLevel(modules);
        assertEquals(5, list.size());
        assertEquals(module1, list.get(0));
        assertEquals(module2, list.get(1));
        assertEquals(module3, list.get(2));
        assertEquals(module5, list.get(3));
        assertEquals(module4, list.get(4));
    }

    public void testNonOptionalDependencyWithUnspecifiedVersionShouldBeTakenIntoAccount() throws Exception {
        final Map modules = new HashMap();
        final ModuleDefinition module1 = new ModuleDefinition("module1", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module2 = new ModuleDefinition("module2", Version.parseVersion("3.0"), null, null);

        module1.addDependency(new DependencyDefinition("module2", null, false));
        
        modules.put("module1", module1);
        modules.put("module2", module2);
        depChecker.checkDependencies(modules);
        final List<ModuleDefinition> sorted = depChecker.sortByDependencyLevel(modules);
        assertEquals(2, sorted.size());
        assertEquals(module2, sorted.get(0));
        assertEquals(module1, sorted.get(1));
    }

    public void testOptionalDependencyWithUnspecifiedVersionShouldBeTakenIntoAccount() throws Exception {
        final Map modules = new HashMap();
        final ModuleDefinition module1 = new ModuleDefinition("module1", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module2 = new ModuleDefinition("module2", Version.parseVersion("3.0"), null, null);

        module1.addDependency(new DependencyDefinition("module2", null, false));

        modules.put("module1", module1);
        modules.put("module2", module2);
        depChecker.checkDependencies(modules);
        final List<ModuleDefinition> sorted = depChecker.sortByDependencyLevel(modules);
        assertEquals(2, sorted.size());
        assertEquals(module2, sorted.get(0));
        assertEquals(module1, sorted.get(1));
    }

    public void testBlowupExplicitelyInCaseOfSelfDependency() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", Version.parseVersion("1"), "fake.Module", null);
        modDefA.setDisplayName("Module-A");
        final DependencyDefinition depOnSelf = new DependencyDefinition();
        depOnSelf.setName("mod-a");
        depOnSelf.setVersion("1");
        modDefA.addDependency(depOnSelf);
        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefA.getName(), modDefA);
        try {
            depChecker.checkDependencies(map);
            fail("should have failed");
        } catch (Throwable e) {
            assertTrue("Should have failed with a ModuleDependencyException instead of " + e.toString(), ModuleDependencyException.class.equals(e.getClass()));
            assertEquals("Cyclic dependency between Module-A (version 1.0.0) and Module-A (version 1.0.0)", e.getMessage());
        }
    }

    public void testCyclicDependenciesBlowupWithAClearExceptionMessage() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", Version.parseVersion("1"), "fake.Module", null);
        modDefA.setDisplayName("Module-A");
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", Version.parseVersion("1"), "fake.Module", null);
        modDefB.setDisplayName("Module-B");
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefA.addDependency(depOnB);
        modDefB.addDependency(depOnA);
        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefA.getName(), modDefA);
        map.put(modDefB.getName(), modDefB);
        try {
            depChecker.checkDependencies(map);
            fail("should have failed");
        } catch (Throwable e) {
            assertTrue("Should have failed with a ModuleDependencyException instead of " + e.toString(), ModuleDependencyException.class.equals(e.getClass()));
            assertEquals("Cyclic dependency between Module-A (version 1.0.0) and Module-B (version 1.0.0)", e.getMessage());
        }
    }

    public void testCoreIsAlwaysSortedFirst() {
        final Map modules = new HashMap();
        final ModuleDefinition core = new ModuleDefinition("core", Version.parseVersion("1.2.3"), null, null);
        final ModuleDefinition module1 = new ModuleDefinition("a_module1", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module2 = new ModuleDefinition("a_module2", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module3 = new ModuleDefinition("a_module3", Version.parseVersion("3.0"), null, null);

        module2.addDependency(new DependencyDefinition("a_module1", "3.0", false));
        module3.addDependency(new DependencyDefinition("core", "3.0", false));

        modules.put("a_module3", module3);
        modules.put("a_module2", module2);
        modules.put("a_module1", module1);
        modules.put("core", core);

        final List list = depChecker.sortByDependencyLevel(modules);
        assertEquals(4, list.size());
        assertEquals(core, list.get(0));
        assertEquals(module1, list.get(1));
        assertEquals(module2, list.get(2));
        assertEquals(module3, list.get(3));
    }

    public void testWebappIsAlwaysSortedLast() {
        final Map modules = new HashMap();
        final ModuleDefinition webapp = new ModuleDefinition("webapp", Version.parseVersion("1.2.3"), null, null);
        final ModuleDefinition core = new ModuleDefinition("core", Version.parseVersion("1.2.3"), null, null);
        final ModuleDefinition module1 = new ModuleDefinition("a_module1", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module2 = new ModuleDefinition("a_module2", Version.parseVersion("3.0"), null, null);
        final ModuleDefinition module3 = new ModuleDefinition("a_module3", Version.parseVersion("3.0"), null, null);

        module2.addDependency(new DependencyDefinition("a_module1", "3.0", false));
        module3.addDependency(new DependencyDefinition("core", "3.0", false));

        modules.put("a_module3", module3);
        modules.put("a_module2", module2);
        modules.put("a_module1", module1);
        modules.put("core", core);
        modules.put("webapp", webapp);

        final List list = depChecker.sortByDependencyLevel(modules);
        assertEquals(5, list.size());
        assertEquals(core, list.get(0));
        assertEquals(module1, list.get(1));
        assertEquals(module2, list.get(2));
        assertEquals(module3, list.get(3));
        assertEquals(webapp, list.get(4));
    }

    private Map buildModulesMapWithDependencyOn(String dependencyDefinitionVersion) {
        final Map modules = new HashMap();
        modules.put("module1", new ModuleDefinition("module1", Version.parseVersion("3.0"), null, null));
        final ModuleDefinition module2 = new ModuleDefinition("module2", Version.parseVersion("1.2"), null, null);
        module2.addDependency(new DependencyDefinition("module1", dependencyDefinitionVersion, false));
        modules.put("module2", module2);
        return modules;
    }

}
