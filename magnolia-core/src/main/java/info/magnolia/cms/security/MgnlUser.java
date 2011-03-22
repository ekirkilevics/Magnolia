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
import static info.magnolia.cms.security.SecurityConstants.*;
import info.magnolia.context.MgnlContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User for 5.0 instance
 * In difference from old MgnlUser, this class operates directly on JCR session and with JCR nodes/properties as our hierarchy managers are not
 * available at the login time.
 * Also in difference from MgnlUser, this class doesn't keep around instance of the user node! TODO: Test performance impact of such change.
 * @author had
 * @version $Id$
 */
public class MgnlUser extends AbstractUser implements User, Serializable {

    private static final long serialVersionUID = 222L;

    private static final boolean logAdmin = false;
    private static final Logger log = LoggerFactory.getLogger(MgnlUser.class);

    private final Map<String, String> properties;
    private final Collection<String> groups;
    private final Collection<String> roles;

    private final String name;
    private Subject subject;
    private final String language;
    private final String encodedPassword;
    private boolean enabled = true;
    private String path;

    private final String realm;

    public MgnlUser(String name, String realm, Collection<String> groups, Collection<String> roles, Map<String, String> properties) {
        this.name = name;
        this.roles = Collections.unmodifiableCollection(roles);
        this.groups = Collections.unmodifiableCollection(groups);
        this.properties = Collections.unmodifiableMap(properties);
        this.realm = realm;

        //shortcut some often accessed props so we don't have to search hashmap for them.
        language = properties.get(MgnlUserManager.PROPERTY_LANGUAGE);
        String enbld = properties.get(MgnlUserManager.PROPERTY_ENABLED);
        // all accounts are enabled by default and prop doesn't exist if the account was not disabled before
        enabled = enbld == null ? true : Boolean.parseBoolean(properties.get(MgnlUserManager.PROPERTY_ENABLED));
        encodedPassword = properties.get(MgnlUserManager.PROPERTY_PASSWORD);
    }

    /**
     * Is this user in a specified group?
     * @param groupName the name of the group
     * @return true if in group
     */
    public boolean inGroup(String groupName) {
        if (logAdmin || !"admin".equals(name)) {
            log.info("inGroup({})", groupName);
        }
        return this.hasAny(groupName, NODE_GROUPS);
    }

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("removeGroup({})", groupName);
        }
        throw new UnsupportedOperationException("use manager to remove groups!");
    }

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    public void addGroup(String groupName) throws UnsupportedOperationException {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("addGroup({})", groupName);
        }
        throw new UnsupportedOperationException("use manager to add groups!");
    }

    public boolean isEnabled() {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("isEnabled()");
        }
        return enabled ;
    }

    /**
     * This methods sets flag just on the bean. It does not update persisted user data. Use manager to update user data.
     */
    public void setEnabled(boolean enabled) {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("setEnabled({})", enabled);
        }
        this.enabled = enabled;
    }

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName) {
        return SecuritySupport.Factory.getInstance().getUserManager(getRealm()).hasAny(getName(), roleName, NODE_ROLES);
    }

    public void removeRole(String roleName) throws UnsupportedOperationException {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("removeRole({})", roleName);
        }
        throw new UnsupportedOperationException("use manager to remove roles!");
    }

    public void addRole(String roleName) throws UnsupportedOperationException {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("addRole({})", roleName);
        }
        throw new UnsupportedOperationException("use manager to add roles!");
    }

    // TODO: methods like the ones below should not be in the object but rather in the manager, making object reusable with different managers.
    private boolean hasAny(final String name, final String nodeName) {
        long start = System.currentTimeMillis();
        try {
            String sessionName;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                sessionName = ContentRepository.USER_ROLES;
            } else {
                sessionName = ContentRepository.USER_GROUPS;
            }

            // TODO: this is an original code. If you ever need to speed it up, turn it around - retrieve group or role by its name and read its ID, then loop through IDs this user has assigned to find out if he has that one or not.
            final Collection<String> groupsOrRoles = MgnlContext.doInSystemContext(new SilentSessionOp<Collection<String>>(ContentRepository.USERS) {

                @Override
                public Collection<String> doExec(Session session) throws RepositoryException {
                    Node groupsOrRoles = session.getNode(getName()).getNode(nodeName);
                    List<String> list = new ArrayList<String>();
                    for (PropertyIterator props = groupsOrRoles.getProperties(); props.hasNext();) {
                        // check for the existence of this ID
                        Property property = props.nextProperty();
                        try {
                            list.add(property.getString());
                        } catch (ItemNotFoundException e) {
                            log.debug("Role [{}] does not exist in the ROLES repository", name);
                        } catch (IllegalArgumentException e) {
                            log.debug("{} has invalid value", property.getPath());
                        }
                    }
                    return list;
                }});


            return MgnlContext.doInSystemContext(new SessionOp<Boolean, RepositoryException>(sessionName) {

                @Override
                public Boolean exec(Session session) throws RepositoryException {
                    for (String groupOrRole : groupsOrRoles) {
                        // check for the existence of this ID
                        try {
                            if (session.getNodeByIdentifier(groupOrRole).getName().equalsIgnoreCase(name)) {
                                return true;
                            }
                        } catch (ItemNotFoundException e) {
                            log.debug("Role [{}] does not exist in the ROLES repository", name);
                        }
                    }
                    return false;
                }});

        } catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            //TODO: why are we swallowing exceptions silently here?
        } finally {
            log.debug("checked {} for {} in {}ms.", new Object[] {name, nodeName, (System.currentTimeMillis() - start)});
        }
        return false;
    }

    public String getName() {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getName()=>{}", name);
        }
        return name;
    }

    public String getPassword() {
        // TODO: should we really decode pwd here? Encoding is UM implementation specific
        return decodePassword(encodedPassword);
    }

    protected String decodePassword(String encodedPassword) {
        return new String(Base64.decodeBase64(encodedPassword.getBytes()));
    }

    public String getLanguage() {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getLang()=>{}", language);
        }
        return this.language;
    }

    public String getProperty(String propertyName) {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getProperty({})", propertyName);
        }
        return properties.get(propertyName);
    }

    public Collection<String> getGroups() {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getGroups()");
        }
        return groups;
    }

    public Collection<String> getAllGroups() {
        // TODO: if the user is just a simple bean, then this method doesn't belong here anymore!!!!
        // should be moved to user manager or to group manager???
        if (logAdmin || !"admin".equals(name)) {
            log.debug("get groups for {}", getName());
        }
        final Set<String> allGroups = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final Collection<String> groups = getGroups();

        // FYI: can't initialize upfront as the instance of the user class needs to be created BEFORE repo is ready
        GroupManager man = SecuritySupport.Factory.getInstance().getGroupManager();

        // add all direct user groups
        allGroups.addAll(groups);

        // add all subbroups
        addSubgroups(allGroups, man, groups);
        return allGroups;
    }

    public Collection<String> getRoles() {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getRoles()");
        }
        return roles;
    }

    public Collection<String> getAllRoles() {
        // TODO: if the user is just a simple bean, then this method doesn't belong here anymore!!!!
        if (logAdmin || !"admin".equals(name)) {
            log.debug("get roles for {}", getName());
        }
        final Set<String> allRoles = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final Collection<String> roles = getRoles();

        // add all direct user groups
        allRoles.addAll(roles);

        Collection<String> allGroups = getAllGroups();

        // FYI: can't initialize upfront as the instance of the user class needs to be created BEFORE repo is ready
        GroupManager man = SecuritySupport.Factory.getInstance().getGroupManager();

        // add roles from all groups
        for (String group : allGroups) {
            try {
                allRoles.addAll(man.getGroup(group).getRoles());
            } catch (AccessDeniedException e) {
                log.debug("Skipping denied group " + group + " for user " + getName(), e);
            } catch (UnsupportedOperationException e) {
                log.debug("Skipping unsupported  getGroup() for group " + group + " and user " + getName(), e);
            }
        }
        return allRoles;
    }

    @Override
    public Subject getSubject() {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getSubject()=>{}", this.subject);
        }
        return this.subject;
    }

    @Override
    @Deprecated
    public void setSubject(Subject subject) {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("setsubject({})", subject);
        }
        this.subject = subject;
    }

    public String getPath() {
        return this.path;
    }

    @Deprecated
    public void setPath(String path) {
        this.path = path;
    }

    private void addSubgroups(final Set<String> allGroups, GroupManager man, Collection<String> groups) {
        for (String group : groups) {
            // check if this group was not already added to prevent infinite loops
            if (!allGroups.contains(group)) {
                try {
                    Collection<String> subgroups = man.getGroup(group).getGroups();
                    allGroups.addAll(subgroups);
                    // and recursively add more subgroups
                    addSubgroups(allGroups, man, subgroups);
                } catch (AccessDeniedException e) {
                    log.debug("Skipping denied group " + group + " for user " + getName(), e);
                } catch (UnsupportedOperationException e) {
                    log.debug("Skipping unsupported  getGroup() for group " + group + " and user " + getName(), e);
                }

            }
        }
    }

    public String getRealm() {
        return realm;
    }

    /**
     * Update the "last access" timestamp.
     * @deprecated since 5.0, use {@link UserManager#updateLastAccessTimestamp(User)} instead
     */
    @Deprecated
    public void setLastAccess() {
        throw new UnsupportedOperationException("Use manager to update user details.");
    }

    /**
     * Not every user needs to have a node behind. Use manager to obtain nodes
     * @deprecated since 5.0, use {@link UserManager#updateLastAccessTimestamp(User)} instead
     */
    @Deprecated
    public Content getUserNode() {
        throw new UnsupportedOperationException("Underlying storage node is no longer exposed nor required for custom user stores.");
    }

    /**
     * @deprecated since 5.0, use {@link UserManager} instead
     */
    @Deprecated
    public void setProperty(String propertyName, String value) {
        throw new UnsupportedOperationException("Use manager to modify properties of the user.");
    }
}
