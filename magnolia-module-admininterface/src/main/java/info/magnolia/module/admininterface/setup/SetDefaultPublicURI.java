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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.TaskExecutionException;

/**
 * Sets the default virtual URI on public instances.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SetDefaultPublicURI extends AbstractTask {
    private static final String DEFAULT_URI_NODEPATH = "/modules/adminInterface/virtualURIMapping/default";
    private final String moduleDescriptorPropertyName;

    public SetDefaultPublicURI(final String moduleDescriptorPropertyName) {
        super("Default URI", "Sets the default virtual URI on public instances.");
        this.moduleDescriptorPropertyName = moduleDescriptorPropertyName;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final String defaultURI = ctx.getCurrentModuleDefinition().getProperty(moduleDescriptorPropertyName);
        final SetPropertyTask setPropertyTask = new SetPropertyTask(ContentRepository.CONFIG, DEFAULT_URI_NODEPATH, "toURI", defaultURI);
        final IsAuthorInstanceDelegateTask task = new IsAuthorInstanceDelegateTask(null, null, null, setPropertyTask);
        task.execute(ctx);
    }
}
