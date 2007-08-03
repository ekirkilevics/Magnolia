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

import freemarker.template.TemplateException;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.model.ModuleDefinition;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerUITest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.ENGLISH);
        replay(context);
        MgnlContext.setInstance(context);
    }

    public void testDoneTemplate() throws IOException, TemplateException {
        final ModuleDefinition mod1 = new ModuleDefinition("foo", "1.0", null, null);
        final ModuleDefinition mod2 = new ModuleDefinition("bar", "2.0", null, null);

        final InstallContextImpl ctx = new InstallContextImpl();
        ctx.setCurrentModule(mod1);
        ctx.info("trala");
        ctx.info("trululu");
        ctx.warn("boo !");
        ctx.setCurrentModule(mod2);
        ctx.error("hoolala", new Exception("booh-ooh!"));
        final ModuleManager moduleManager = createStrictMock(ModuleManager.class);
        final ModuleManager.ModuleManagementState state = new ModuleManager.ModuleManagementState();
        expect(moduleManager.getStatus()).andReturn(state);
        expect(moduleManager.getInstallContext()).andReturn(ctx);
        final StringWriter out = new StringWriter();

        replay(moduleManager);

        new ModuleManagerUI("/bibabu").render("done", moduleManager, out);
        // just checking model and template work properly together...

        verify(moduleManager);
    }

}
