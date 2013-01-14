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
package info.magnolia.cms.core;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.cache.GrowingLRUMap;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permissions granting access to all users to root Caches the result of resolving paths from ids, the caching implementation based {@link org.apache.jackrabbit.core.security.authorization.principalbased.ACLProvider.CompiledPermissionImpl}. See {@link MagnoliaAccessProvider#canAccessRoot()} for details.
 */
public class RootOnlyPermissions extends DefaultACLBasedPermissions {

    @SuppressWarnings("unchecked")
    private final Map<ItemId, Boolean> readCache = new GrowingLRUMap(1024, 5000);
    private final Object monitor = new Object();

    private static final Logger log = LoggerFactory.getLogger(RootOnlyPermissions.class);

    public RootOnlyPermissions(SessionImpl systemSession) {
        super(null, systemSession, null);
        this.session = systemSession;
    }

    @Override
    public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {

        if (itemPath == null) {

            // we deal only with permissions on nodes
            if (!itemId.denotesNode()) {
                itemId = ((PropertyId) itemId).getParentId();
            }

            synchronized (monitor) {

                if (readCache.containsKey(itemId)) {
                    return readCache.get(itemId);
                }

                itemPath = session.getHierarchyManager().getPath(itemId);

                boolean canRead = "/".equals(pathResolver.getJCRPath(itemPath));
                readCache.put(itemId, canRead);
                return canRead;
            }
        }

        String path = pathResolver.getJCRPath(itemPath);
        log.debug("Read request for " + path + " :: " + itemId);
        return "/".equals(pathResolver.getJCRPath(itemPath));
    }

    @Override
    public boolean grants(Path absPath, int permissions) throws RepositoryException {
        return "/".equals(pathResolver.getJCRPath(absPath));
    }
}
