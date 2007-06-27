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
import static org.easymock.EasyMock.*;

import java.util.Arrays;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDeployerImplTest extends TestCase {

    /**
     * TODO : should check that d1 is actually called before d2
     */
    public void testUpdateAppliesSuppliedDeltas() throws Exception {
        final InstallContext ctx = createStrictMock(InstallContext.class);
        final Delta d1 = createStrictMock(Delta.class);
        final Delta d2 = createStrictMock(Delta.class);
        final ModuleVersionHandler moduleVersionHandler = createStrictMock(ModuleVersionHandler.class);
        final Version fromVersion = new Version("1.2.3");
        final Version newVersion = new Version("2.3.4");

        expect(moduleVersionHandler.getLatestVersion()).andReturn(newVersion);
        expect(moduleVersionHandler.getDeltas(fromVersion, newVersion)).andReturn(Arrays.asList(d1, d2));
        d2.apply(ctx);
        d1.apply(ctx);

        replay(ctx, d1, d2, moduleVersionHandler);

        final ModuleDeployerImpl moduleDeployer = new ModuleDeployerImpl(ctx);
        moduleDeployer.update(moduleVersionHandler, fromVersion);

        verify(ctx, d1, d2, moduleVersionHandler);
    }
}
