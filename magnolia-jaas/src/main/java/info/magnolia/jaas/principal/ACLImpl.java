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
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.ACL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * This class represents access control list as a principal.
 * @author Sameer Charles $Id$
 */
public class ACLImpl implements ACL {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String NAME = "acl";

    private String name;

    private List list;

    private String repository;

    private String workspace;

    public ACLImpl() {
        this.list = new ArrayList();
    }

    /**
     * Get name given to this principal.
     * @return name
     */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return NAME;
        }
        return this.name;
    }

    /**
     * Set this principal name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get repository ID for which this ACL has been constructed.
     * @return repository ID
     */
    public String getRepository() {
        return this.repository;
    }

    /**
     * Set repository ID for which this ACL will be constructed.
     * @param repository
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Get workspace ID for which this ACL has been constructed.
     * @return workspace ID
     */
    public String getWorkspace() {
        return this.workspace;
    }

    /**
     * Set workspace ID for which this ACL will be constructed.
     * @param workspace
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * Adds permission in to the list of permissions for this principal.
     * @param permission
     */
    public void addPermission(Object permission) {
        this.list.add(permission);
    }

    /**
     * Initialize access control list with provided permissions it will overwrite any existing permissions set before.
     * @param list
     */
    public void setList(List list) {
        this.list.clear();
        this.list.addAll(list);
    }

    /**
     * Returns list of permissions for this principal. Returned list is not a copy and should be treated as read only!
     */
    public List getList() {
        return this.list;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", this.name).append(
            "workspace",
            this.workspace).append("repository", this.repository).append("list", this.list).toString();
    }

}
