/**
 * This file Copyright (c) 2007-2011 Magnolia International
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
package info.magnolia.module.admininterface.setup.for4_0;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Iterator;

/**
 * In 4.0 and 4.1, we accidentally overrode the default uri mapping on public instances to the "Quickstart" page,
 * even if templates were present.
 * This task simply warns the user if that hasn't been fixed in the meantime.
 */
public class UpdatedDefaultPublicURIWarning extends AbstractRepositoryTask {

    public UpdatedDefaultPublicURIWarning() {
        super("Checks the default public URI is correct", "Warns the user if the URI has been changed wrongly.");
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        if (templatesExist() && hasDefaultURI(installContext)) {
            installContext.warn("Please set the default virtual URI mapping; it was incorrectly reset by a previous update.");
        }
    }

    /**
     * Check if at least one template is visible in the system.
     */
    private boolean templatesExist() {
        Collection nodeCollection = QueryUtil.query(ContentRepository.CONFIG, "select * from mgnl:content where jcr:path like '/modules/%/templates'");
        Iterator nodesCollectionIt = nodeCollection.iterator();
        while (nodesCollectionIt.hasNext()) {
            Content templatesNode = (Content) nodesCollectionIt.next();
            Iterator templatesCollectionIt = templatesNode.getChildren(ItemType.CONTENTNODE).iterator();
            while (templatesCollectionIt.hasNext()) {
                Content template = (Content) templatesCollectionIt.next();
                if (NodeDataUtil.getBoolean(template, "visible", false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDefaultURI(InstallContext installContext) throws RepositoryException {
        final String defaultURI = installContext.getCurrentModuleDefinition().getProperty("defaultPublicURI");

        final String defaultUriMappingPath = "/modules/adminInterface/virtualURIMapping/default/toURI";
        final HierarchyManager hm = installContext.getConfigHierarchyManager();
        if (hm.isExist(defaultUriMappingPath)) {
            final String currentValue = hm.getNodeData(defaultUriMappingPath).getString();
            if (StringUtils.equals(currentValue, defaultURI)) {
                return true;
            }
        }
        return false;
    }

}
