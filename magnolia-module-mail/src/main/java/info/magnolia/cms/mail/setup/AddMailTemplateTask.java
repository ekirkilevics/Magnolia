/**
 * This file Copyright (c) 2008 Magnolia International
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
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.Task;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddMailTemplateTask extends ArrayDelegateTask {


    public AddMailTemplateTask(String templateName, String description, String from, String subject, String templatePath, String type) {
        super("Mail template", "Adds the " + templateName + " " + description + ".", new Task[]{
                new CreateNodeTask(null, null, ContentRepository.CONFIG, "/modules/mail/config/templates", templateName, ItemType.CONTENTNODE.getSystemName()),
                new NewPropertyTask(null, null, ContentRepository.CONFIG, "/modules/mail/config/templates/" + templateName, "from", from),
                new NewPropertyTask(null, null, ContentRepository.CONFIG, "/modules/mail/config/templates/" + templateName, "subject", subject),
                new NewPropertyTask(null, null, ContentRepository.CONFIG, "/modules/mail/config/templates/" + templateName, "body", templatePath),
                new NewPropertyTask(null, null, ContentRepository.CONFIG, "/modules/mail/config/templates/" + templateName, "type", type)
        });
    }
}
