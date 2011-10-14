/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module;

import org.junit.Test;

import info.magnolia.module.model.ModuleDefinition;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test case for ModuleLifecycleEventDispatcher.
 */
public class ModuleLifecycleEventDispatcherTest {

    public ModuleLifecycleEventDispatcher dispatcher = new ModuleLifecycleEventDispatcher();

    @Test
    public void testGlobalListener() {

        ModuleDefinition md = new ModuleDefinition();
        md.setName("bogus");

        ModuleLifecycleListener listener = mock(ModuleLifecycleListener.class);
        dispatcher.addListener(listener);

        dispatcher.fireStarted(md, null);
        verify(listener, times(1)).onModuleStarted(md, null);

        dispatcher.fireRestarted(md, null);
        verify(listener, times(1)).onModuleRestarted(md, null);

        dispatcher.fireStopped(md, null);
        verify(listener, times(1)).onModuleStopped(md, null);

        dispatcher.fireStarted(md, null);
        verify(listener, times(2)).onModuleStarted(md, null);

        dispatcher.fireRestarted(md, null);
        verify(listener, times(2)).onModuleRestarted(md, null);

        dispatcher.fireStopped(md, null);
        verify(listener, times(2)).onModuleStopped(md, null);
    }

    @Test
    public void testModuleSpecificListener() {

        ModuleDefinition bogusModule = new ModuleDefinition();
        bogusModule.setName("bogus");
        ModuleDefinition foobarModule = new ModuleDefinition();
        foobarModule.setName("foobar");

        ModuleLifecycleListener globalListener = mock(ModuleLifecycleListener.class);
        dispatcher.addListener(globalListener);

        ModuleLifecycleListener specificListener = mock(ModuleLifecycleListener.class);
        dispatcher.addListener("foobar", specificListener);

        dispatcher.fireStarted(bogusModule, null);
        verify(globalListener, times(1)).onModuleStarted(bogusModule, null);
        verify(specificListener, times(0)).onModuleStarted(bogusModule, null);

        dispatcher.fireStarted(foobarModule, null);
        verify(globalListener, times(1)).onModuleStarted(foobarModule, null);
        verify(specificListener, times(1)).onModuleStarted(foobarModule, null);
    }

    @Test
    public void testRemoveListener() {

        ModuleDefinition bogusModule = new ModuleDefinition();
        bogusModule.setName("bogus");

        ModuleLifecycleListener globalListener = mock(ModuleLifecycleListener.class);
        dispatcher.addListener(globalListener);

        dispatcher.fireStarted(bogusModule, null);
        verify(globalListener, times(1)).onModuleStarted(bogusModule, null);

        dispatcher.removeListener(globalListener);

        dispatcher.fireStarted(bogusModule, null);
        dispatcher.fireStarted(bogusModule, null);
        dispatcher.fireStarted(bogusModule, null);
        // listener will still only have seen 1 event
        verify(globalListener, times(1)).onModuleStarted(bogusModule, null);
    }

    @Test
    public void testReRegisterListener() {

        ModuleLifecycleListener listener = mock(ModuleLifecycleListener.class);
        dispatcher.addListener("foobar", listener);

        ModuleDefinition bogusModule = new ModuleDefinition();
        bogusModule.setName("bogus");
        ModuleDefinition foobarModule = new ModuleDefinition();
        foobarModule.setName("foobar");

        // listener doesn't listen to 'bogus'
        dispatcher.fireStarted(bogusModule, null);
        verify(listener, times(0)).onModuleStarted(bogusModule, null);

        // but it listens to 'foobar'
        dispatcher.fireStarted(foobarModule, null);
        verify(listener, times(1)).onModuleStarted(foobarModule, null);

        // listener re-registered for 'bogus'
        dispatcher.addListener("bogus", listener);

        // now will receive 'bogus'
        dispatcher.fireStarted(bogusModule, null);
        verify(listener, times(1)).onModuleStarted(bogusModule, null);

        // but no longer 'foobar'
        dispatcher.fireStarted(foobarModule, null);
        verify(listener, times(1)).onModuleStarted(foobarModule, null);
    }
}
