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
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DependencyCheckerTest extends TestCase {
    private DependencyChecker depChecker;

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
            assertEquals("Module module2 version 1.2 is dependent on module1 version 1.0/2.0, but module1 version 3.0 is currently installed.", e.getMessage());
        }
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfLowerBound() {
        final Map modules = buildModulesMapWithDependencyOn("4.0/5.0");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 version 1.2 is dependent on module1 version 4.0/5.0, but module1 version 3.0 is currently installed.", e.getMessage());
        }
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfUpperBoundWithInfiniteLowerBound() {
        final Map modules = buildModulesMapWithDependencyOn("*/2.0");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 version 1.2 is dependent on module1 version */2.0, but module1 version 3.0 is currently installed.", e.getMessage());
        }
    }

    public void testDependenciesShouldBeInvalidIfOutsideOfLowerBoundWithInfiniteUpperBound() {
        final Map modules = buildModulesMapWithDependencyOn("4.0/*");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 version 1.2 is dependent on module1 version 4.0/*, but module1 version 3.0 is currently installed.", e.getMessage());
        }
    }

    public void testShouldFailWhenDependencyNotFound() {
        final Map modules = buildModulesMapWithDependencyOn("Q.W");
        modules.remove("module1");
        try {
            depChecker.checkDependencies(modules);
            fail("should have failed");
        } catch (ModuleDependencyException e) {
            assertEquals("Module module2 version 1.2 is dependent on module1 version Q.W, which was not found.", e.getMessage());
        }
    }

    public void testModulesShouldBeSortedAccordingToDependencies() {
        final Map modules = new HashMap();
        final ModuleDefinition module1 = new ModuleDefinition("module1", "3.0", null, null);
        final ModuleDefinition module2 = new ModuleDefinition("module2", "3.0", null, null);
        module2.addDependency(new DependencyDefinition("module1", "3.0", false));
        final ModuleDefinition module3 = new ModuleDefinition("module3", "3.0", null, null);
        module3.addDependency(new DependencyDefinition("module1", "3.0", false));
        module3.addDependency(new DependencyDefinition("module2", "3.0", false));
        final ModuleDefinition module4 = new ModuleDefinition("module4", "3.0", null, null);
        module4.addDependency(new DependencyDefinition("module1", "3.0", false));
        module4.addDependency(new DependencyDefinition("module3", "3.0", false));
        final ModuleDefinition module5 = new ModuleDefinition("module5", "3.0", null, null);
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

    public void testCoreIsAlwaysSortedFirst() {
        final Map modules = new HashMap();
        final ModuleDefinition core = new ModuleDefinition("core", "1.2.3", null, null);
        final ModuleDefinition module1 = new ModuleDefinition("a_module1", "3.0", null, null);
        final ModuleDefinition module2 = new ModuleDefinition("a_module2", "3.0", null, null);
        final ModuleDefinition module3 = new ModuleDefinition("a_module3", "3.0", null, null);

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
        final ModuleDefinition webapp = new ModuleDefinition("webapp", "1.2.3", null, null);
        final ModuleDefinition core = new ModuleDefinition("core", "1.2.3", null, null);
        final ModuleDefinition module1 = new ModuleDefinition("a_module1", "3.0", null, null);
        final ModuleDefinition module2 = new ModuleDefinition("a_module2", "3.0", null, null);
        final ModuleDefinition module3 = new ModuleDefinition("a_module3", "3.0", null, null);

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
        modules.put("module1", new ModuleDefinition("module1", "3.0", null, null));
        final ModuleDefinition module2 = new ModuleDefinition("module2", "1.2", null, null);
        module2.addDependency(new DependencyDefinition("module1", dependencyDefinitionVersion, false));
        modules.put("module2", module2);
        return modules;
    }

    protected void setUp() throws Exception {
        super.setUp();
        depChecker = new DependencyChecker();
    }
}
