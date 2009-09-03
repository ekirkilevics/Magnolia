/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.mail.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.mail.commands.MailCommand;
import static info.magnolia.module.mail.setup.MailModuleVersionHandler.COMMAND_IN_ADMININTERFACEMODULE_PATH;
import static info.magnolia.module.mail.setup.MailModuleVersionHandler.MAIL_COMMAND_CLASS_PRIOR_TO_4_0;
import info.magnolia.module.model.Version;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MailModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/mail.xml";
    }

    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new MailModuleVersionHandler() {
            // cheat - the conditions needs web.xml. Can't be bothered to fake that here
            @Override
            protected List mailServletIsWrappedSince_3_5() {
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
