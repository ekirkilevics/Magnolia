/**
 * This file Copyright (c) 2007-2010 Magnolia International
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

import info.magnolia.cms.security.auth.PrincipalList;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implementation of the <code>PrincipalList</code> providing most common functionality.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractPrincipalList implements PrincipalList {

    private String name;

    /**
     * List of role names assigned to a principal.
     */
    private Collection list;

    protected AbstractPrincipalList() {
        this.list = new ArrayList();
    }

    /**
     * Gets name given to this principal.
     * @return name
     */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return getDefaultName();
        }
        return this.name;
    }

    abstract String getDefaultName();

    /**
     * Sets principal name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a name to the list.
     * @param name
     */
    public void add(String name) {
        this.list.add(name);
    }

    /**
     * Gets list of roles as string.
     * @return roles
     */
    public Collection getList() {
        return this.list;
    }

    /**
     * Checks if the role name exist in this list.
     * @param name
     */
    public boolean has(String name) {
        Iterator listIterator = this.list.iterator();
        while (listIterator.hasNext()) {
            String roleName = (String) listIterator.next();
            if (StringUtils.equalsIgnoreCase(name, roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", getName()).append("list", this.list).toString();
    }
}
