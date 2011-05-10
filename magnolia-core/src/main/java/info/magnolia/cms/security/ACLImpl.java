/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.cms.security.auth.ACL;

import java.util.List;

/**
 * Basic ACL implementation.
 * @author had
 * @version $Id: $
 */
public class ACLImpl implements ACL {

    private static final long serialVersionUID = 7683918091476831307L;

    private final List<Permission> permissions;
    private final String workspaceName;
    private final String repositoryName;
    private final String name;

    public ACLImpl(String name, String repositoryName, String workspaceName, List<Permission> permissions) {
        this.name = name;
        this.repositoryName = repositoryName;
        this.workspaceName = workspaceName;
        this.permissions = permissions;
    }

    @Override
    public List<Permission> getList() {
        return permissions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRepository() {
        return repositoryName;
    }

    @Override
    public String getWorkspace() {
        return workspaceName;
    }
}
