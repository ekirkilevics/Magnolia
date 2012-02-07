/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 * Rename renderers to mach name used since 3.5.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RenamedRenderersToTemplateRenderers extends AllModulesNodeOperation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RenamedRenderersToTemplateRenderers.class);

    private static final String OLDNAME = "renderers";
    private static final String NEWNAME = "template-renderers";

    public RenamedRenderersToTemplateRenderers() {
        super("Template renderers", "Modules' renderers nodes were renamed to template-renderers.");
    }

    @Override
    protected void operateOnModuleNode(Content parent, HierarchyManager hm, InstallContext ctx) throws TaskExecutionException {
        final String moduleNodePath = parent.getHandle();
        final String oldPath = moduleNodePath + "/" + OLDNAME;
        final String newPath = moduleNodePath + "/" + NEWNAME;
        if (hm.isExist(oldPath)) {
            log.debug("Will move " + oldPath + " to " + newPath);
            try {
                hm.moveTo(oldPath, newPath);
            } catch (RepositoryException e) {
                ctx.error("Could not rename " + oldPath + " to " + newPath, e);
            }
        }
    }

}
