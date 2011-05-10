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

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Dummy user implementation returned when an actual user instance can't be instantiated.
 *
 * @deprecated since 4.3.6 - usage needs to be reviewed - see MAGNOLIA-3269
 *
 * @author Sameer Charles
 * @version $Revision:9391 $ ($Author:scharles $)
 */
@Deprecated
public class DummyUser extends AbstractUser {

    private static final Logger log = LoggerFactory.getLogger(DummyUser.class);

    private static final String DEFAULT_LANGUAGE = "en";

    public DummyUser() {
        log.info("Initializing dummy user - Anonymous");
        log.info("This area and/or instance is not secured");
    }

    /**
     * Dummy user has full access, always returns true.
     */
    @Override
    public boolean hasRole(String roleName) {
        return true;
    }

    /**
     * Simply log that its a dummy user.
     */
    @Override
    public void removeRole(String roleName) throws UnsupportedOperationException {
        log.debug("User [ Anonymous ] has no roles");
    }

    /**
     * Simply log that its a dummy user.
     * @param roleName the name of the role
     */
    @Override
    public void addRole(String roleName) throws UnsupportedOperationException {
        log.debug("No roles can be attached to user [ Anonymous ]");
    }

    /**
     * Always returns true.
     */
    @Override
    public boolean inGroup(String groupName) {
        return true;
    }

    @Override
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        log.debug("User [ Anonymous ] has no groups");
    }

    @Override
    public void addGroup(String groupName) throws UnsupportedOperationException {
        log.debug("No groups can be attached to user [ Anonymous ]");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public String getLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @Override
    public String getName() {
        return UserManager.ANONYMOUS_USER;
    }

    @Override
    public String getPassword() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getProperty(String propertyName) {
        return null;
    }

    @Override
    public void setProperty(String propertyName, String value) {
        log.debug("Can not set properties on dummy user (name: {}, value: {})", propertyName, value);
    }

    @Override
    public Collection<String> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getAllGroups() {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getAllRoles() {
        return new ArrayList<String>();
    }
}
