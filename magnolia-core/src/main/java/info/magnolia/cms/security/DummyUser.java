package info.magnolia.cms.security;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Date: Oct 26, 2005 Time: 5:02:46 PM
 * @author Sameer Charles
 * @version $Revision:9391 $ ($Author:scharles $)
 */
public class DummyUser implements User {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(DummyUser.class);

    /**
     * Default language
     */
    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * package private constructor
     */
    DummyUser() {
        log.info("Initializing dummy user - Anonymous");
        log.info("This area and/or instance is not secured");
    }

    /**
     * has full access
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName) {
        return true;
    }

    /**
     * Simply log that its a dummy user
     * @param roleName
     */
    public void removeRole(String roleName) throws UnsupportedOperationException {
        log.debug("User [ Anonymous ] has no roles");
    }

    /**
     * Simply log that its a dummy user
     * @param roleName the name of the role
     */
    public void addRole(String roleName) throws UnsupportedOperationException {
        log.debug("No roles can be attached to user [ Anonymous ]");
    }

    /**
     * Is this user in a specified group?
     * @param groupName
     * @return true if in group
     */
    public boolean inGroup(String groupName) {
        return true;
    }

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        log.debug("User [ Anonymous ] has no groups");
    }

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    public void addGroup(String groupName) throws UnsupportedOperationException {
        log.debug("No groups can be attached to user [ Anonymous ]");
    }

    /**
     * get user language
     * @return language string
     */

    public String getLanguage() {
        return DEFAULT_LANGUAGE;
    }

    /**
     * get user name
     * @return name string
     */
    public String getName() {
        return UserManager.ANONYMOUS_USER;
    }

    public String getPassword() {
        return StringUtils.EMPTY;
    }

    public String getProperty(String propertyName) {
        return null;
    }

    public void setProperty(String propertyName, String value) {
        log.debug("Can not set properties on dummy user (name: {}, value: {})", propertyName, value);
    }

    /**
     * @see info.magnolia.cms.security.User#getGroups()
     */
    public Collection getGroups() {
        return Collections.EMPTY_LIST;
    }

    public Collection getAllGroups() {
        return new ArrayList();
    }

    /**
     * @see info.magnolia.cms.security.User#getRoles()
     */
    public Collection getRoles() {
        return Collections.EMPTY_LIST;
    }

    public Collection getAllRoles() {
        return new ArrayList();
    }
}
