/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.mail.commands.MailCommand;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.PropertyValueDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MailModuleVersionHandler extends DefaultModuleVersionHandler {
    static final String MAIL_COMMAND_CLASS_PRIOR_TO_4_0 = "info.magnolia.cms.mail.commands.MailCommand";
    static final String COMMAND_IN_ADMININTERFACEMODULE_PATH = "/modules/adminInterface/commands/default/sendMail";

    public MailModuleVersionHandler() {

        register(DeltaBuilder.update("3.5", "")
                .addTask(new RegisterModuleServletsTask())
                .addConditions(mailServletIsWrappedSince_3_5())
        );

        final CheckAndModifyPropertyValueTask mailServletMapping = new CheckAndModifyPropertyValueTask("Mapping for mail servlet", "Fixes the mapping for the mail servlet, making it specification compliant.",
                ContentRepository.CONFIG,
                "/server/filters/servlets/Mail/mappings/--magnolia-mail-",
                "pattern", "/.magnolia/mail*", "/.magnolia/mail"
        );

        register(DeltaBuilder.update("3.6.2", "")
                .addTask(mailServletMapping)
        );

        final RemoveNodeTask removeMailServletMapping = new RemoveNodeTask("Remove mail servlet", "Removes the mail servlet.",
                ContentRepository.CONFIG,
                "/server/filters/servlets/Mail"
        );

        final RemoveNodeTask replaceConfigMenuMail = new RemoveNodeTask("Remove tools mail menu", "Removes the tools mail menu.",
                ContentRepository.CONFIG,
                "/modules/adminInterface/config/menu/tools/mails"
        );

        final MoveNodeContentTask moveTemplates = new MoveNodeContentTask("Rename templates", "Templates will be renamed to templatesConfiguration.",
                ContentRepository.CONFIG,
                "/modules/mail/config/templates",
                "/modules/mail/config/templatesConfiguration",
                ItemType.CONTENT,
                false);

        register(DeltaBuilder.update("4.0", "")
                .addTask(removeMailServletMapping)
                .addTask(replaceConfigMenuMail)
                .addTask(moveTemplates)
                .addTask(new BootstrapConditionally("Mail handlers", "Installs mail handlers.", "/mgnl-bootstrap/mail/config.modules.mail.config.handler.xml"))
                .addTask(new BootstrapConditionally("Mail page", "Installs mail page.", "/mgnl-bootstrap/mail/config.modules.mail.pages.xml"))
                .addTask(new BootstrapConditionally("Mail factory", "Installs mail factories.", "/mgnl-bootstrap/mail/config.modules.mail.config.factory.xml"))
                .addTask(new BootstrapSingleResource("Mail menu", "Installs mail tools menu.", "/mgnl-bootstrap/mail/config.modules.adminInterface.config.menu.tools.sendMail.xml"))
                .addTask(new CheckAndModifyPropertyValueTask("Mail command", "", ContentRepository.CONFIG, COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", MAIL_COMMAND_CLASS_PRIOR_TO_4_0, MailCommand.class.getName()))
        );

        register(DeltaBuilder.update("4.0.3", "")
                //since 4.0 mail command class was not updated to the new value
                //mail command class was changed on 4.0, needs to be fixed for 4.0.3 and 4.1.1
                .addTask(fixMailCommand(MAIL_COMMAND_CLASS_PRIOR_TO_4_0, MailCommand.class.getName()))
        );

        register(DeltaBuilder.update("4.1.1", "")
              //since 4.0 mail command class was not updated to the new value
              //mail command class was changed on 4.0, needs to be fixed for 4.0.3 and 4.1.1
              .addTask(fixMailCommand(MAIL_COMMAND_CLASS_PRIOR_TO_4_0, MailCommand.class.getName()))
        );
    }

    protected List<Condition> mailServletIsWrappedSince_3_5() {
        final ArrayList<Condition> conditions = new ArrayList<Condition>();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsNowWrapped("Mail");
        return conditions;
    }

    private PropertyValueDelegateTask fixMailCommand(final String previouslyWrongValue, final String fixedValue) {
        final String workspaceName = ContentRepository.CONFIG;
        final CheckAndModifyPropertyValueTask fixTask = new CheckAndModifyPropertyValueTask(null, null, workspaceName, COMMAND_IN_ADMININTERFACEMODULE_PATH,
                "class", previouslyWrongValue, fixedValue);

        return new PropertyValueDelegateTask("Mail Command",
                "Checks and updates the mail command if not correct.",
                workspaceName, COMMAND_IN_ADMININTERFACEMODULE_PATH, "class", previouslyWrongValue, false, fixTask);
    }
}
