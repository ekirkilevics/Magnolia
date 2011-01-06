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
