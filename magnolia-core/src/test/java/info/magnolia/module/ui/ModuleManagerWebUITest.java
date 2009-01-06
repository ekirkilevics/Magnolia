/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.ui;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.freemarker.FreemarkerTemplateLoaderManager;
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
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        FactoryUtil.setInstance(ServerConfiguration.class, serverConfiguration);

        FactoryUtil.setInstance(FreemarkerTemplateLoaderManager.class, new FreemarkerTemplateLoaderManager());
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
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

    public void testListTasksTemplate() throws ModuleManagementException {
        doTestTemplate("listTasks");
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
        MgnlContext.setInstance(context);

        final ModuleDefinition mod1 = new ModuleDefinition("foo", Version.parseVersion("1.0"), null, null);
        final ModuleDefinition mod2 = new ModuleDefinition("bar", Version.parseVersion("2.0"), null, null);

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
