package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.util.Properties;


/**
 * Useful EL functions that can be used in jsp 2.0 pages.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class CmsFunctions {

    /**
     * Returns the current active page (can be set using the loadPage tag).
     * @return current page
     */
    public static Content currentPage() {
        return MgnlContext.getAggregationState().getCurrentContent();
    }

    /**
     * Returns the main loaded page (doesn't change when using the loadPage tag).
     * @return loaded page
     */
    public static Content mainPage() {
        return MgnlContext.getAggregationState().getMainContent();
    }

    /**
     * Returns the current paragraph.
     * @return current paragraph
     */
    public static Content currentParagraph() {
        return Resource.getLocalContentNode();
    }

    /**
     * Output a full url given a content handle (usually a page)
     * @param handle page handle
     * @return url formed using context path + handle + default extension
     */
    public static String link(String handle) {
        return MgnlContext.getContextPath() + handle + '.' + Server.getDefaultExtension();
    }

    /**
     * Returns the value of a system property
     * @param key property key
     * @return property value
     */
    public static String systemProperty(String key) {
        return SystemProperty.getProperty(key);
    }

    /**
     * Returns the system properties
     * @return Property instance
     */
    public static Properties systemProperties() {
        return SystemProperty.getProperties();
    }

    /**
     * Check if a user is currently logged in (not anonymous)
     * @return true if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        User user = MgnlContext.getUser();
        return (user != null && !UserManager.ANONYMOUS_USER.equals(user.getName()));
    }

    /**
     * Check if the current user can edit the active page
     * @return true if the current user can edit the active page.
     */
    public boolean canEdit() {
        return Resource.getActivePage().isGranted(Permission.SET);
    }

}
