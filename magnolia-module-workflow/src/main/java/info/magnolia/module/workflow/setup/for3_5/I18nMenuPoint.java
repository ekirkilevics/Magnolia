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
package info.magnolia.module.workflow.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.PropertyValuesTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class I18nMenuPoint extends PropertyValuesTask {
    private static final String MENU_PATH = "/modules/adminInterface/config/menu/inbox";

    public I18nMenuPoint() {
        super("i18n", "Internationalization of the Inbox menu entry.");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager("config");
        if (!hm.isExist(MENU_PATH)) {
            ctx.warn("Inbox menu does not exist at " + MENU_PATH);
        }
        try {
            final Content menu = hm.getContent(MENU_PATH);
            checkAndModifyPropertyValue(ctx, menu, "label", "Inbox", "menu.inbox");
            newProperty(ctx, menu, "i18nBasename", "info.magnolia.module.workflow.messages");
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not execute task: " + e.getMessage(), e);
        }
    }
}
