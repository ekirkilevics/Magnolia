/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.mail.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;

import java.util.ArrayList;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MailModuleVersionHandler extends DefaultModuleVersionHandler {

    public MailModuleVersionHandler() {
        final ArrayList conditions = new ArrayList();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsNowWrapped("Mail");

        register(DeltaBuilder.update("3.5", "")
                .addTask(new RegisterModuleServletsTask())
                .addConditions(conditions)
        );

        final CheckAndModifyPropertyValueTask mailServletMapping = new CheckAndModifyPropertyValueTask("Mapping for mail servlet", "Fixes the mapping for the mail servlet, making it specification compliant.",
                ContentRepository.CONFIG,
                "/server/filters/servlets/Mail/mappings/--magnolia-mail-",
                "pattern", "/.magnolia/mail*", "/.magnolia/mail"
        );

        register(DeltaBuilder.update("3.6.2", "")
                .addTask(mailServletMapping)
        );

        final RemoveNodeTask removeMailServletMapping = new RemoveNodeTask("New mail page", "Deletes old mail servlet",
                ContentRepository.CONFIG,
                "/server/filters/servlets/Mail"
        );

        final RemoveNodeTask removeConfigMenuMail = new RemoveNodeTask("New mail page", "Deletes old menu item mail",
                ContentRepository.CONFIG,
                "/modules/adminInterface/config/menu/tools/mails"
        );

        final MoveNodeTask moveTemplates = new MoveNodeTask("Rename templates", "templates will be templatesConfiguration",
                ContentRepository.CONFIG,
                "/modules/mail/config/templates",
                "/modules/mail/config/templatesConfiguation",
                false);


        final CheckAndModifyPropertyValueTask changeTemplatesNodeType = new CheckAndModifyPropertyValueTask("Change templates type", "Templates node will become of type content",
                ContentRepository.CONFIG,
                "/modules/mail/config/templatesConfiguration",
                "jcr:primaryType",
                ItemType.CONTENTNODE.getSystemName(),
                ItemType.CONTENT.getSystemName()
        );

        register(DeltaBuilder.update("4.0", "")
                .addTask(removeMailServletMapping)
                .addTask(removeConfigMenuMail)
                .addTask(moveTemplates)
                .addTask(changeTemplatesNodeType)
        );

    }
}
