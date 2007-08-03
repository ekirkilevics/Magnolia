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

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleLifecycleContextImplTest extends TestCase {
    public void testCantRegisterAComponentIfNodeNameIsAlreadyForAnotherComponent() {
        final ModuleLifecycleContextImpl lifecycleCtx = new ModuleLifecycleContextImpl();
        lifecycleCtx.registerModuleObservingComponent("foo", new DummyObservedManager1());
        try {
            lifecycleCtx.registerModuleObservingComponent("foo", new DummyObservedManager2());
            fail("should have failed");
        } catch (IllegalStateException e) { // TODO : maybe we just need to log, or throw a more specific exception ?
            assertEquals("ObservedManager DummyObservedManager1 was already registered for nodes of name foo, DummyObservedManager2 can't be registered.", e.getMessage());
        }
    }

    public void testStartRegistersAllObserversWithAllModules() {
        
    }

    private static class DummyObservedManager1 extends ObservedManager {

        protected void onRegister(Content node) {
        }

        protected void onClear() {
        }

        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private static class DummyObservedManager2 extends DummyObservedManager1 {
    }
}
