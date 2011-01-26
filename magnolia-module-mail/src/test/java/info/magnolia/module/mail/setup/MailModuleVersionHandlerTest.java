/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.module.mail.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.mail.commands.MailCommand;
import info.magnolia.module.model.Version;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static info.magnolia.module.mail.setup.MailModuleVersionHandler.COMMAND_IN_ADMININTERFACEMODULE_PATH;
import static info.magnolia.module.mail.setup.MailModuleVersionHandler.MAIL_COMMAND_CLASS_PRIOR_TO_4_0;

/**
 * MailModuleVersionHandlerTest.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MailModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/mail.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/mail.xml",
                "/META-INF/magnolia/admininterface.xml",
                "/META-INF/magnolia/templating.xml",
                "/META-INF/magnolia/core.xml"
        );
    }

    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new MailModuleVersionHandler() {
            // cheat - the conditions needs web.xml. Can't be bothered to fake that here
            @Override
            protected List<Condition> mailServletIsWrappedSince_3_5() {
                return Collections.emptyList();
            }
        };
    }

    public void testMailCommandIsSetProperlyOnInstallWithoutWarning() throws Exception {
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);
        assertConfig(MailCommand.class.getName(), COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");
        assertNoMessages(ctx);
    }

    // yes, 3.7 does not exist; this is just to avoid all 3.5 and 3.6 tasks
    public void testUpdatingFrom37() throws Exception {
        setupConfigProperty("/modules/mail/config/templates", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/Mail", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/tools/mails", "foo", "bar");
        setupConfigProperty(COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", MAIL_COMMAND_CLASS_PRIOR_TO_4_0);
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.7"));
        assertConfig(MailCommand.class.getName(), COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");
        assertNoMessages(ctx);
    }

    // yes, 3.7 does not exist; this is just to avoid all 3.5 and 3.6 tasks
    public void testUpdatingFrom37WithCustomCommand() throws Exception {
        setupConfigProperty("/modules/mail/config/templates", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/Mail", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/tools/mails", "foo", "bar");
        setupConfigProperty(COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", "custom-command-class");
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.7"));
        assertConfig("custom-command-class", COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");

        // TODO - this isn't exactly the best message; if a user has modified this class, they probably did it on purpose
        // a simple warning that the original class name has changed would probably be more appropriate
        assertSingleMessage(ctx, "Property \"class\" was expected to exist at /modules/adminInterface/commands/default/sendMail with value \"info.magnolia.cms.mail.commands.MailCommand\" but has the value \"custom-command-class\" instead.", InstallContext.MessagePriority.warning);
    }

    public void testUpdatingFrom402ShouldBeSilentIfUserAlreadyFixedTheirConfig() throws Exception {
        setupConfigProperty("/modules/mail/config/templates", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/Mail", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/tools/mails", "foo", "bar");
        setupConfigProperty(COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", MailCommand.class.getName());
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.2"));
        assertConfig(MailCommand.class.getName(), COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");
        assertNoMessages(ctx);
    }

    public void testUpdatingFrom41ShouldBeSilentIfUserAlreadyFixedTheirConfig() throws Exception {
        setupConfigProperty("/modules/mail/config/templates", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/Mail", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/tools/mails", "foo", "bar");
        setupConfigProperty(COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", MailCommand.class.getName());
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));
        assertConfig(MailCommand.class.getName(), COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");
        assertNoMessages(ctx);

    }

    public void testUpdatingFrom402ShouldBeSilentEvenWithCustomCommandClass() throws Exception {
        setupConfigProperty("/modules/mail/config/templates", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/Mail", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/tools/mails", "foo", "bar");
        setupConfigProperty(COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", "custom-command");
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.2"));
        assertConfig("custom-command", COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");
        assertNoMessages(ctx);
    }

    public void testUpdatingFrom41ShouldBeSilentEvenWithCustomCommandClass() throws Exception {
        setupConfigProperty("/modules/mail/config/templates", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/Mail", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/tools/mails", "foo", "bar");
        setupConfigProperty(COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", "custom-command");
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));
        assertConfig("custom-command", COMMAND_IN_ADMININTERFACEMODULE_PATH + "/class");
        assertNoMessages(ctx);
    }
}
