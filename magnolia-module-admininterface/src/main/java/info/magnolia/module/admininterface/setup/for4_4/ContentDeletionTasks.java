/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admininterface.setup.for4_4;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;

/**
 * Collection of tasks necessary for enabling controlled deletion of content.
 * @author had
 * @version $Id: $
 */
public class ContentDeletionTasks extends ArrayDelegateTask implements Task {

    public ContentDeletionTasks() {
        super("Content Deletion", "Makes configuration changes necessary for content deletion workflow support.",
                new RegisterMgnlDeletedType(),
                new BootstrapSingleResource("Delete Command", "Installs new delete command to allow for activation of deletions.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.commands.website.delete.xml"),
                new BootstrapSingleResource("", "", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.templates.mgnlDeleted.xml"),
                // disable delete confirmation since we also install the command to not delete immediately
                new SetPropertyTask(ContentRepository.CONFIG, "/modules/adminInterface/trees/website", "enableDeleteConfirmation", "false")
        );
    }
}
