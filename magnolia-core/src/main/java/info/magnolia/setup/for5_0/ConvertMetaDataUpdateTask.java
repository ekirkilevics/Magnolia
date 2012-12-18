/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.setup.for5_0;

import java.util.Collection;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.importexport.postprocessors.MetaDataAsMixinConversionHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;

/**
 * Update task for migration content with MetaData sub nodes to instead use mixins on the content node itself.
 *
 * @see MetaDataAsMixinConversionHelper
 */
public class ConvertMetaDataUpdateTask extends AbstractRepositoryTask {

    private final Logger log = LoggerFactory.getLogger(ConvertMetaDataUpdateTask.class);

    public ConvertMetaDataUpdateTask(String taskName, String taskDescription) {
        super(taskName, taskDescription);
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {

        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);

        Collection<String> workspaceNames = repositoryManager.getWorkspaceNames();
        for (String workspaceName : workspaceNames) {
            Session session = installContext.getJCRSession(workspaceName);

            MetaDataAsMixinConversionHelper conversionHelper = new MetaDataAsMixinConversionHelper();
            conversionHelper.setDeleteMetaDataIfEmptied(true);
            conversionHelper.setPeriodicSaves(true);

            conversionHelper.convertNodeAndChildren(session.getRootNode());

            log.debug("Converted MetaData in workspace [{0}]", workspaceName);
        }
    }
}
