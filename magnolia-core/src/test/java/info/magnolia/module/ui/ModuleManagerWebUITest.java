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
package info.magnolia.module.ui;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.util.FactoryUtil;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.io.StringWriter;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerWebUITest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        // shunt log4j
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("info.magnolia");
        logger.setLevel(org.apache.log4j.Level.OFF);

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        FactoryUtil.setInstance(ServerConfiguration.class, serverConfiguration);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testModuleManagementExceptionsArePropagatedEvenThoughTheUpdateIsRunningInASeparateThread() throws ModuleManagementException, InterruptedException {
        final InstallContextImpl ctx = new InstallContextImpl();
        final ModuleManager moduleManager = createStrictMock(ModuleManager.class);
        moduleManager.performInstallOrUpdate();
        expectLastCall().andThrow(new IllegalStateException("boo!"));
        expect(moduleManager.getInstallContext()).andReturn(ctx);

        replay(moduleManager);
        final ModuleManagerWebUI ui = new ModuleManagerWebUI(moduleManager);
        ui.performInstallOrUpdate();
        Thread.sleep(1000);
        verify(moduleManager);

        assertEquals(1, ctx.getMessages().size());
        // "General messages" is the key used when adding message without a current module being set in the context
        final List messagesForNoModule = ((List) ctx.getMessages().get("General messages"));
        assertEquals(1, messagesForNoModule.size());
        final InstallContext.Message msg = (InstallContext.Message) messagesForNoModule.get(0);
        assertEquals("Could not perform installation: boo!", msg.getMessage());
        assertEquals(InstallContext.MessagePriority.error, msg.getPriority());
    }

    public void testDoneTemplate() throws ModuleManagementException {
        doTestTemplate("installDone");
    }
    public void testRestartNeededTemplate() throws ModuleManagementException {
        doTestTemplate("installDoneRestartNeeded");
    }

    private void doTestTemplate(String templateName) throws ModuleManagementException {
        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.ENGLISH);
        expect(context.getContextPath()).andReturn("/bibabu");
        expect(context.getServletContext()).andReturn(null);
        MgnlContext.setInstance(context);

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

        replay(context, moduleManager);

        new ModuleManagerWebUI(moduleManager).render(templateName, out);
        // just checking model and template work properly together...

        verify(context, moduleManager);
    }

}
