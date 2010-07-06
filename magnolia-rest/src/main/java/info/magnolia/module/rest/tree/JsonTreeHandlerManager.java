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
package info.magnolia.module.rest.tree;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;

import javax.jcr.RepositoryException;

public class JsonTreeHandlerManager {

    public TreeHandler getTreeHandler(String treeName) throws RepositoryException, Content2BeanException {

        TreeHandler treeHandler = instantiateHandlerFromRepository(treeName);

        // If there's no handler by that name, try to have it bootstrapped from old admininterface
        if (treeHandler == null)
            if (new LegacyTreeHandlerFactory().bootstrapLegacyTreeHandler(treeName))
                treeHandler = instantiateHandlerFromRepository(treeName);

        return treeHandler;
    }

    private TreeHandler instantiateHandlerFromRepository(String treeName) throws RepositoryException, Content2BeanException {

        // For now read a fresh instance from repository, later on this will be done by a ObservedManager

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        String configPath = "/modules/rest/genuine-trees/" + treeName;
        if (hierarchyManager.isExist(configPath)) {
            Content content = hierarchyManager.getContent(configPath);
            return (TreeHandler) Content2BeanUtil.toBean(content, true, ConfiguredTreeHandler.class);
        }
        return null;
    }

    public static JsonTreeHandlerManager getInstance() {
        return Components.getSingleton(JsonTreeHandlerManager.class);
    }
}
