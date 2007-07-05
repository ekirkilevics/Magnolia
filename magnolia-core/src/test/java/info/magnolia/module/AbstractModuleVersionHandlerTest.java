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

import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.Version;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createNiceMock;

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
    private DummyModuleVersionHandler versionHandler;

    protected void setUp() throws Exception {
        super.setUp();
        d1 = createNiceMock(Delta.class);
        d2 = createNiceMock(Delta.class);
        d3 = createNiceMock(Delta.class);
        d4 = createNiceMock(Delta.class);
        d5 = createNiceMock(Delta.class);
        versionHandler = new DummyModuleVersionHandler();
        versionHandler.register(new Version("1.0.0"), d1);
        versionHandler.register(new Version("1.0.1"), d2);
        versionHandler.register(new Version("1.1"), d3);
        versionHandler.register(new Version("1.2"), d4);
        versionHandler.register(new Version("1.3"), d5);
    }

    public void testRetrievesTheAppropriateListOfDeltas() {
        final List deltas = versionHandler.getDeltas(new Version("1.0.1"), new Version("1.2"));
        assertEquals(2, deltas.size());
        assertEquals(d3, deltas.get(0));
        assertEquals(d4, deltas.get(1));
    }

    public void testReturnsEmptyListIfNoDeltaWasRegisteredForNewerVersion() {
        final List deltas = versionHandler.getDeltas(new Version("1.3"), new Version("1.4"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testReturnsEmptyListIfNoDeltaWasRegisteredForIntermediateVersions() {
        final List deltas = versionHandler.getDeltas(new Version("1.1"), new Version("1.1.1"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testReturnsEmptyListIfNoDeltaWasRegisteredAtAll() {
        final DummyModuleVersionHandler versionHandler = new DummyModuleVersionHandler();
        final List deltas = versionHandler.getDeltas(new Version("1.0.1"), new Version("1.2"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testDeltasAreSorted() {
        // yes, this test might pass by accident.
        final List deltas = versionHandler.getDeltas(new Version("0.4"), new Version("2.0"));
        assertEquals(5, deltas.size());
        assertEquals(d1, deltas.get(0));
        assertEquals(d2, deltas.get(1));
        assertEquals(d3, deltas.get(2));
        assertEquals(d4, deltas.get(3));
        assertEquals(d5, deltas.get(4));
    }

    private final static class DummyModuleVersionHandler extends AbstractModuleVersionHandler {
        protected DummyModuleVersionHandler() {
            super(null);
        }
    }
}
