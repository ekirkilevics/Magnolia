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
package info.magnolia.module;

import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.model.Version;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AbstractModuleVersionHandlerTest extends TestCase {
    private Delta d1;
    private Delta d2;
    private Delta d3;
    private Delta d4;
    private Delta d5;
    private AbstractModuleVersionHandler versionHandler;

    protected void setUp() throws Exception {
        super.setUp();
        d1 = createNiceMock(Delta.class);
        d2 = createNiceMock(Delta.class);
        d3 = createNiceMock(Delta.class);
        d4 = createNiceMock(Delta.class);
        d5 = createNiceMock(Delta.class);
        expect(d1.getVersion()).andReturn(Version.parseVersion("1.0.0"));
        expect(d2.getVersion()).andReturn(Version.parseVersion("1.0.1"));
        expect(d3.getVersion()).andReturn(Version.parseVersion("1.1"));
        expect(d4.getVersion()).andReturn(Version.parseVersion("1.2"));
        expect(d5.getVersion()).andReturn(Version.parseVersion("1.3"));
        expect(d1.getTasks()).andReturn(new ArrayList());
        expect(d2.getTasks()).andReturn(new ArrayList());
        expect(d3.getTasks()).andReturn(new ArrayList());
        expect(d4.getTasks()).andReturn(new ArrayList());
        expect(d5.getTasks()).andReturn(new ArrayList());
        replay(d1, d2, d3, d4, d5);
        versionHandler = new DefaultModuleVersionHandler() {};
        versionHandler.register(d1);
        versionHandler.register(d2);
        versionHandler.register(d3);
        versionHandler.register(d4);
        versionHandler.register(d5);
    }

    public void testCantRegisterMultipleDeltasForSameVersion() {
        final Delta d1 = DeltaBuilder.update(Version.parseVersion("1.0.0"), "", new NullTask("", ""));
        final Delta d2 = DeltaBuilder.update(Version.parseVersion("1.0.0"), "", new NullTask("", ""));
        final AbstractModuleVersionHandler versionHandler = new DefaultModuleVersionHandler() {};
        versionHandler.register(d1);
        try {
            versionHandler.register(d2);
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Version 1.0.0 was already registered in this ModuleVersionHandler.", e.getMessage());
        }
    }

    public void testRetrievesTheAppropriateListOfDeltas() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("1.0.1"));//, new Version("1.2"));
        assertEquals(3, deltas.size());
        assertEquals(d3, deltas.get(0));
        assertEquals(d4, deltas.get(1));
        assertEquals(d5, deltas.get(2));
    }

    public void testRetrievesTheAppropriateDeltaForIntermediateUnregisteredVersion() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("1.2.5"));//, new Version("1.2"));
        assertEquals(1, deltas.size());
        assertEquals(d5, deltas.get(0));
    }

    public void testReturnsEmptyListIfNoDeltaWasRegisteredForNewerVersion() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("1.4"));//, new Version("1.4"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testReturnsEmptyListIfLatestDeltaWasRegisteredForCurrentVersion() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("1.3"));//, new Version("1.4"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

//    public void testReturnsEmptyListIfNoDeltaWasRegisteredForIntermediateVersions() {
//        final List deltas = versionHandler.getDeltas(new Version("1.2.5"));//, new Version("1.1.1"));
//        assertNotNull(deltas);
//        assertEquals(0, deltas.size());
//    }

    public void testReturnsEmptyListIfNoDeltaWasRegisteredAtAll() {
        final AbstractModuleVersionHandler versionHandler = new DefaultModuleVersionHandler() {};
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("1.0.1"));//, new Version("1.2"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testDeltasAreSorted() {
        // yes, this test might pass by accident.
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("0.4"));//, new Version("2.0"));
        assertEquals(5, deltas.size());
        assertEquals(d1, deltas.get(0));
        assertEquals(d2, deltas.get(1));
        assertEquals(d3, deltas.get(2));
        assertEquals(d4, deltas.get(3));
        assertEquals(d5, deltas.get(4));
    }

    public void testVersionUpdateTaskIsAddedWhenUsingSingleTaskMethod() {
        final NullTask nullTask = new NullTask("test", "test");
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "", nullTask);
        doTestVersionUpdateTaskIsAdded(delta, 2);
    }

    public void testVersionUpdateTaskIsAddedWhenAddingListOfTasks() {
        final NullTask nullTask = new NullTask("test", "test");
        final NullTask nullTask2 = new NullTask("test2", "test2");
        final ArrayList tasks = new ArrayList();
        tasks.add(nullTask);
        tasks.add(nullTask2);
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "").addTasks(tasks);
        doTestVersionUpdateTaskIsAdded(delta, 3);
    }

    public void testVersionUpdateTaskIsAddedWhenAddingSingleTasks() {
        final NullTask t1 = new NullTask("test", "test");
        final NullTask t2 = new NullTask("test2", "test2");
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "").addTask(t1).addTask(t2);
        doTestVersionUpdateTaskIsAdded(delta, 3);
    }

    public void testVersionUpdateTaskIsAddedWhenUsingSingleTaskMethodAndAddingMore() {
        final NullTask t1 = new NullTask("test", "test");
        final NullTask t2 = new NullTask("test2", "test2");
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "", t1).addTask(t2);
        doTestVersionUpdateTaskIsAdded(delta, 3);
    }

    public void testVersionUpdateTaskIsAddedEvenIfNoTask() {
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "");
        doTestVersionUpdateTaskIsAdded(delta, 1);
    }

    private void doTestVersionUpdateTaskIsAdded(Delta delta, int expectedTotal) {
        final AbstractModuleVersionHandler versionHandler = new DefaultModuleVersionHandler() {
        };
        versionHandler.register(delta);
        final List list = versionHandler.getDeltas(new InstallContextImpl(), Version.parseVersion("1.0"));
        assertEquals(1, list.size());
        final Delta retrievedDelta = (Delta) list.get(0);
        final List tasks = retrievedDelta.getTasks();
        assertEquals(expectedTotal, tasks.size());
        for (int i = 0; i < expectedTotal - 1; i++) {
            assertTrue(tasks.get(i) instanceof NullTask);
        }
        assertTrue(tasks.get(expectedTotal - 1) instanceof AbstractModuleVersionHandler.ModuleVersionUpdateTask);
    }

    private final static class NullTask extends AbstractTask {
        public NullTask(String name, String description) {
            super(name, description);
        }

        public void execute(InstallContext installContext) {
        }
    }

}
