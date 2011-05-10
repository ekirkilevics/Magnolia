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
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.PrincipalCollection;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Principal collection implementation based on simple list. Collection is managed externally.
 * Date: Jun 29, 2005
 * @author Sameer Charles $Id$
 */
public class PrincipalCollectionImpl implements PrincipalCollection {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String NAME = "PrincipalCollection";

    /**
     * Collection of principal objects.
     */
    private final Collection<Principal> collection = new ArrayList<Principal>();

    private String name;

    /**
     * Gets name given to this principal.
     * @return name
     */
    @Override
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return NAME;
        }
        return this.name;
    }

    /**
     * Sets this principal name.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets internal collection of principals.
     * @deprecated since 5.0
     */
    @Override
    @Deprecated
    public void set(Collection<Principal> collection) {
        throw new UnsupportedOperationException("use addAll() instead");
    }

    /**
     * Adds principal to the internal collection of principals.
     * @param principal to be added to the collection
     */
    @Override
    public void add(Principal principal) {
        this.collection.add(principal);
    }

    /**
     * Adds principals to the internal collection of principals.
     * @param principal to be added to the collection
     */
    @Override
    public void addAll(Collection<Principal> principal) {
        this.collection.addAll(principal);
    }

    /**
     * Removes principal from the collection if present or does nothing in case principal was not present in the collection.
     * @param principal to be removed from the collection
     */
    @Override
    public void remove(Principal principal) {
        this.collection.remove(principal);
    }

    /**
     * removes all principals from the collection.
     */
    @Override
    public void clearAll() {
        this.collection.clear();
    }

    /**
     * Checks if this collection contains specified principal.
     * @return true if the specified object exist in the collection, false otherwise.
     */
    @Override
    public boolean contains(Principal principal) {
        return this.collection.contains(principal);
    }

    /**
     * Checks if this collection contains principal with the specified name.
     * @param name
     * @return true if the collection contains the principal by the specified name, false otherwise.
     */
    @Override
    public boolean contains(String name) {
        return this.get(name) != null;
    }

    /**
     * Gets principal associated to the specified name from the collection.
     * @param name
     * @return principal object associated to the specified name.
     */
    @Override
    public Principal get(String name) {
        //TODO: change internal collection to a map and store names as keys to avoid loops !!!!
        Iterator<Principal> principalIterator = this.collection.iterator();
        while (principalIterator.hasNext()) {
            Principal principal = principalIterator.next();
            if (StringUtils.equalsIgnoreCase(name, principal.getName())) {
                return principal;
            }
        }
        return null;
    }

    /**
     * Returns an iterator over the collection of principals.
     * @return iterator for Principal objects
     */
    @Override
    public Iterator<Principal> iterator() {
        return collection.iterator();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", this.name).toString();
    }
}
