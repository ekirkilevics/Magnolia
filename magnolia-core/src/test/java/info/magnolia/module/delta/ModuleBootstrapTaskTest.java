/**
 * This file Copyright (c) 2007-2008 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleBootstrapTaskTest extends TestCase {

    public void testShouldOnlyBootstrapFilesFromThisModule() {
        final ModuleDefinition modDef = new ModuleDefinition("test-module", Version.parseVersion("1.0"), null, null);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getCurrentModuleDefinition()).andReturn(modDef).anyTimes();
        replay(ctx);

        final ModuleBootstrapTask task = new ModuleBootstrapTask();
        assertEquals(true, task.acceptResource(ctx, "/mgnl-bootstrap/test-module/foo.xml"));
        assertEquals(true, task.acceptResource(ctx, "/mgnl-bootstrap/test-module/foo/bar.xml"));
        assertEquals(false, task.acceptResource(ctx, "/mgnl-bootstrap/test-module-foo.xml"));
        assertEquals(false, task.acceptResource(ctx, "/mgnl-bootstrap/test-module.xml"));
        assertEquals(false, task.acceptResource(ctx, "/mgnl-bootstrap/other-module/foo/bar.xml"));
        assertEquals(false, task.acceptResource(ctx, "/mgnl-pouet/test-module/foo/bar.xml"));
        assertEquals(false, task.acceptResource(ctx, "/mgnl-pouet/other-module/foo/bar.xml"));
        assertEquals(false, task.acceptResource(ctx, "/test-module/foo.xml"));
        assertEquals(false, task.acceptResource(ctx, "/other-module/foo.xml"));
        verify(ctx);
    }

}
