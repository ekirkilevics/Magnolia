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

import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.NullTask;
import info.magnolia.module.delta.Task;
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
        expect(d1.getTasks()).andReturn(new ArrayList());
        expect(d2.getTasks()).andReturn(new ArrayList());
        expect(d3.getTasks()).andReturn(new ArrayList());
        expect(d4.getTasks()).andReturn(new ArrayList());
        expect(d5.getTasks()).andReturn(new ArrayList());
        replay(d1, d2, d3, d4, d5);
        versionHandler = new DefaultModuleVersionHandler() {};
        versionHandler.register(new Version("1.0.0"), d1);
        versionHandler.register(new Version("1.0.1"), d2);
        versionHandler.register(new Version("1.1"), d3);
        versionHandler.register(new Version("1.2"), d4);
        versionHandler.register(new Version("1.3"), d5);
    }

    public void testCantRegisterMultipleDeltasForSameVersion() {
        final Delta d1 = new BasicDelta("", "", new NullTask("", ""));
        final Delta d2 = new BasicDelta("", "", new NullTask("", ""));
        final AbstractModuleVersionHandler versionHandler = new DefaultModuleVersionHandler() {};
        versionHandler.register(new Version("1.0.0"), d1);
        try {
            versionHandler.register(new Version("1.0.0"), d2);
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Version 1.0.0 was already registered in this ModuleVersionHandler.", e.getMessage());
        }
    }

    public void testCantRegisterMultipleDeltasForSameVersionEvenIfRegisteredWithString() {
        final Delta d1 = new BasicDelta("", "", new NullTask("", ""));
        final Delta d2 = new BasicDelta("", "", new NullTask("", ""));
        final AbstractModuleVersionHandler versionHandler = new DefaultModuleVersionHandler() {};
        versionHandler.register(new Version("1.1.0"), d1);
        try {
            versionHandler.register("1.1.0", d2);
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Version 1.1.0 was already registered in this ModuleVersionHandler.", e.getMessage());
        }
    }

    public void testRetrievesTheAppropriateListOfDeltas() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), new Version("1.0.1"));//, new Version("1.2"));
        assertEquals(3, deltas.size());
        assertEquals(d3, deltas.get(0));
        assertEquals(d4, deltas.get(1));
        assertEquals(d5, deltas.get(2));
    }

    public void testRetrievesTheAppropriateDeltaForIntermediateUnregisteredVersion() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), new Version("1.2.5"));//, new Version("1.2"));
        assertEquals(1, deltas.size());
        assertEquals(d5, deltas.get(0));
    }

    public void testReturnsEmptyListIfNoDeltaWasRegisteredForNewerVersion() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), new Version("1.4"));//, new Version("1.4"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testReturnsEmptyListIfLatestDeltaWasRegisteredForCurrentVersion() {
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), new Version("1.3"));//, new Version("1.4"));
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
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), new Version("1.0.1"));//, new Version("1.2"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testDeltasAreSorted() {
        // yes, this test might pass by accident.
        final List deltas = versionHandler.getDeltas(new InstallContextImpl(), new Version("0.4"));//, new Version("2.0"));
        assertEquals(5, deltas.size());
        assertEquals(d1, deltas.get(0));
        assertEquals(d2, deltas.get(1));
        assertEquals(d3, deltas.get(2));
        assertEquals(d4, deltas.get(3));
        assertEquals(d5, deltas.get(4));
    }

    public void testVersionUpdateTaskIsAddedWhenUsingBasicDeltaListConstructor() {
        final NullTask nullTask = new NullTask("test", "test");
        final ArrayList tasks = new ArrayList();
        tasks.add(nullTask);
        final Delta delta = new BasicDelta("", "", tasks);
        doTestVersionUpdateTaskIsAddedWhenUsingBasicDeltaConstructor(delta);
    }

    public void testVersionUpdateTaskIsAddedWhenUsingBasicDeltaArrayConstructor() {
        final NullTask nullTask = new NullTask("test", "test");
        final Task[] tasks = new Task[]{nullTask};
        final Delta delta = new BasicDelta("", "", tasks);
        doTestVersionUpdateTaskIsAddedWhenUsingBasicDeltaConstructor(delta);
    }

    public void testVersionUpdateTaskIsAddedWhenUsingBasicDeltaSingleTaskConstructor() {
        final NullTask nullTask = new NullTask("test", "test");
        final Delta delta = new BasicDelta("", "", nullTask);
        doTestVersionUpdateTaskIsAddedWhenUsingBasicDeltaConstructor(delta);
    }

    private void doTestVersionUpdateTaskIsAddedWhenUsingBasicDeltaConstructor(Delta delta) {
        final AbstractModuleVersionHandler versionHandler = new DefaultModuleVersionHandler() {};
        versionHandler.register("2.0", delta);
        final List list = versionHandler.getDeltas(new InstallContextImpl(), new Version("1.0"));
        assertEquals(1, list.size());
        final Delta retrievedDelta = (Delta) list.get(0);
        final List tasks = retrievedDelta.getTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.get(0) instanceof NullTask);
        assertTrue(tasks.get(1) instanceof AbstractModuleVersionHandler.ModuleVersionUpdateTask);
    }
}
