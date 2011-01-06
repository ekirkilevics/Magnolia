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
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the users stored in the {@link ContentRepository#USER_ROLES} workspace.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlRoleManager implements RoleManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlRoleManager.class);

    /**
     * Do not instantiate it!
     */
    public MgnlRoleManager() {
    }

    public Role getRole(String name) {
        try {
            return newRoleInstance(getHierarchyManager().getContent(name));
        }
        catch (Exception e) {
            log.info("can't find role [" + name + "]", e);
            return null;
        }
    }

    public Role createRole(String name) {
        try {
            Content node = getHierarchyManager().createContent("/", name, ItemType.ROLE.getSystemName());
            getHierarchyManager().save();
            return newRoleInstance(node);
        }
        catch (Exception e) {
            log.error("can't create role [" + name + "]", e);
            return null;
        }
    }

    protected MgnlRole newRoleInstance(Content node) {
        return new MgnlRole(node);
    }

    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(ContentRepository.USER_ROLES);
    }
}
