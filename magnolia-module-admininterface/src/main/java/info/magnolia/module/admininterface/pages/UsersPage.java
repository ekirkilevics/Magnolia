/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class UsersPage extends TemplatedMVCHandler {

    /**
     * Required constructor.
     * @param name page name
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public UsersPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    // TODO - this is not dependent on the structure of the users workspace but will only work when using MgnlUserManagers.
    public Collection getUserNodes() throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.USERS);
        final Content root = hm.getRoot();
        // needed so collectAllChildren goes through folders
        final List allIncludingFolders = ContentUtil.collectAllChildren(root, new ItemType[]{ItemType.FOLDER, ItemType.USER});
        // now we need to remove the folders - TODO : the page itself (UsersPage.html) could instead do this sorting and display folders differently...
        return CollectionUtils.select(allIncludingFolders, new Predicate() {
            public boolean evaluate(Object object) {
                return ((Content) object).isNodeType(ItemType.USER.getSystemName());
            }
        });
    }

}
