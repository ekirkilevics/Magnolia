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
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleBootstrapTaskTest extends TestCase {

    public void testShouldOnlyBootstrapFilesFromThisModule() {
        final ModuleDefinition modDef = new ModuleDefinition("test-module", "1.0", null, null);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getCurrentModuleDefinition()).andReturn(modDef).anyTimes();
        replay(ctx);

        final ModuleBootstrapTask task = new ModuleBootstrapTask();
        assertEquals(true, task.acceptResource(ctx, "/mgnl-bootstrap/test-module/foo.xml"));
        assertEquals(false, task.acceptResource(ctx, "/mgnl-bootstrap/test-module/foo/bar.xml"));
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
