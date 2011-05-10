/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import java.io.ByteArrayInputStream;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.Provider;

/**
 * Task for registration of new mixin - mgnl:deleted.
 * @author had
 * @version $Id: $
 */
public class RegisterMgnlDeletedType extends AbstractTask {

    public RegisterMgnlDeletedType() {
        super("", "");
    }

    @Override
    public void execute(InstallContext ctx) throws TaskExecutionException {
        HierarchyManager webHM = ctx.getHierarchyManager(ContentRepository.WEBSITE);
        final String mixDeleted = "mgnl:deleted";

        try {
            webHM.getWorkspace().getNodeTypeManager().getNodeType(mixDeleted);
            // if we are here the type is already registered
            return;
        } catch (RepositoryException e) {
            // wish there was a better way to check node type existence in the
            // API ...
        }

        Provider repoProvider = ContentRepository.getRepositoryProvider(ContentRepository.WEBSITE);
        String mgnlMixDeleted = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<nodeTypes" + " xmlns:rep=\"internal\""
                + " xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"" + " xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
                + " xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\"" + " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">" + "<nodeType name=\"" + mixDeleted
                + "\" isMixin=\"true\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">" + "<supertypes>" + "<supertype>nt:base</supertype>"
                + "</supertypes>" + "</nodeType>" + "</nodeTypes>";

        try {
            repoProvider.registerNodeTypes(new ByteArrayInputStream(mgnlMixDeleted.getBytes()));
        } catch (RepositoryException e) {
            ctx.error("Failed to register mgnl:mixDeleted mixin. Please update node type definition manually before proceeding", e);
        }

    }
}