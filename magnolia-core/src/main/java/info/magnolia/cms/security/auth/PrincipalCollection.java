/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.security.auth;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Iterator;


/**
 * A collection of {@link Principal principals}.
 * @author Sameer Charles $Id$
 */
public interface PrincipalCollection extends Principal, Serializable, Iterable<Principal> {

    @Override
    public String getName();

    public void setName(String name);

    public void set(Collection<Principal> collection);

    public void add(Principal principal);

    public void addAll(Collection<Principal> principal);

    public void remove(Principal principal);

    public void clearAll();

    public boolean contains(Principal principal);

    @Override
    public Iterator<Principal> iterator();

    /**
     * Checks if this collection contains object with the specified name.
     */
    public boolean contains(String name);

    /**
     * Get principal associated to the specified name from the collection.
     */
    public Principal get(String name);

}
