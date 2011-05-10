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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;


/**
 * A group implementation. This bean is not connected to the underlying group storage.
 * @author Sameer Charles $Id$
 */
public class MgnlGroup implements Group {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroup.class);

    private final String name;
    private final Collection<String> groups;
    private final Collection<String> roles;
    private final String id;

    public MgnlGroup(String id, String groupName, Collection<String> subgroupNames, Collection<String> roleNames) {
        this.id = id;
        this.name = groupName;
        this.groups = Collections.unmodifiableCollection(subgroupNames);
        this.roles = Collections.unmodifiableCollection(roleNames);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void addRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        throw new UnsupportedOperationException("Use manager to modify this group");
    }

    /**
     * Add a subgroup to this group.
     */
    @Override
    public void addGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException {
        throw new UnsupportedOperationException("Use manager to modify this group");
    }

    @Override
    public void removeRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        throw new UnsupportedOperationException("Use manager to modify this group");
    }

    /**
     * Remove a subgroup from this group.
     */
    @Override
    public void removeGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException {
        throw new UnsupportedOperationException("Use manager to modify this group");
    }

    @Override
    public boolean hasRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        return this.roles.contains(roleName);
    }

    /**
     * FIXME: Yet another method that is questionable. Either we take all arbitrary properties of the group and load them in memory,
     * wasting the time and memory on group instance creation or such props should be requested on demand from the manager.
     * The fact that group interface exposes also setter for this method makes it even worse!
     */
    @Override
    public String getProperty(String propertyName) {
        throw new UnsupportedOperationException("Use manager to retrieve arbitrary group properties");
    }

    @Override
    public void setProperty(String propertyName, String value) {
        throw new UnsupportedOperationException("Use manager to modify this group");
    }

    /**
     * Returns read only roles collection.
     * @see info.magnolia.cms.security.Group#getRoles()
     */
    @Override
    public Collection<String> getRoles() {
        return Collections.unmodifiableCollection(this.roles);
    }

    /**
     * Returns read only groups collection.
     * @see info.magnolia.cms.security.Group#getGroups()
     */
    @Override
    public Collection<String> getGroups() {
        return this.groups;
    }

    /**
     * FIXME: While this method could be potentially supported and can return all the groups that this group belongs to by inheritance,
     * it doesn't seem to be great idea to pre-fill it on object creation and should be just requested on demand from manager.
     */
    @Override
    public Collection<String> getAllGroups() {
        throw new UnsupportedOperationException("Use manager to modify this group");
    }

    @Override
    public String getId() {
        return id;
    }
}
