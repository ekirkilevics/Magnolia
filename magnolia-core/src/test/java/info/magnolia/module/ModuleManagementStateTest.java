/**
 * This file Copyright (c) 2010 Magnolia International
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

import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.DeltaType;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagementStateTest extends TestCase {
    private static final String[] TEXTS = new String[]{"installs only", "updates only", "installs and updates"};
    private static final Version v01 = Version.parseVersion("0.1");
    private static final Version v05 = Version.parseVersion("0.5");
    private static final Version v10 = Version.parseVersion("1.0");

    public void testDescriptionForUpdatesOnly() {
        final ModuleManager.ModuleManagementState state = new ModuleManager.ModuleManagementState();
        state.addModule(new ModuleDefinition("a", v10, null, null), v01, Arrays.<Delta>asList(
                DeltaBuilder.update(v10, "").addTask(new WarnTask("", "")),
                DeltaBuilder.update(v05, "").addTask(new WarnTask("", ""))
        ));
        state.addModule(new ModuleDefinition("b", v10, null, null), v01, Arrays.<Delta>asList(
                DeltaBuilder.update(v10, "").addTask(new WarnTask("", "")),
                DeltaBuilder.update(v05, "").addTask(new WarnTask("", ""))
        ));

        assertEquals(1, state.getDeltaTypes().size());
        assertTrue(state.getDeltaTypes().contains(DeltaType.update));
        assertEquals("updates only", state.getDeltaTypesDescription(TEXTS));
    }

    public void testDescriptionForInstallsOnly() {
        final ModuleManager.ModuleManagementState state = new ModuleManager.ModuleManagementState();
        state.addModule(new ModuleDefinition("a", v10, null, null), null, Arrays.<Delta>asList(
                DeltaBuilder.install(v10, "").addTask(new WarnTask("", ""))
        ));
        state.addModule(new ModuleDefinition("b", v10, null, null), null, Arrays.<Delta>asList(
                DeltaBuilder.install(v10, "").addTask(new WarnTask("", ""))
        ));

        assertEquals(1, state.getDeltaTypes().size());
        assertTrue(state.getDeltaTypes().contains(DeltaType.install));
        assertEquals("installs only", state.getDeltaTypesDescription(TEXTS));
    }

    public void testDescriptionForInstallsAndUpdates() {
        final ModuleManager.ModuleManagementState state = new ModuleManager.ModuleManagementState();
        state.addModule(new ModuleDefinition("a", v10, null, null), v01, Arrays.<Delta>asList(
                DeltaBuilder.update(v10, "").addTask(new WarnTask("", "")),
                DeltaBuilder.update(v05, "").addTask(new WarnTask("", ""))
        ));
        state.addModule(new ModuleDefinition("b", v10, null, null), null, Arrays.<Delta>asList(
                DeltaBuilder.install(v10, "").addTask(new WarnTask("", ""))
        ));

        assertEquals(2, state.getDeltaTypes().size());
        assertTrue(state.getDeltaTypes().contains(DeltaType.install));
        assertTrue(state.getDeltaTypes().contains(DeltaType.update));
        assertEquals("installs and updates", state.getDeltaTypesDescription(TEXTS));

    }
}
