package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SecurityUtil;
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
    public static boolean isLoggedIn() {
        return SecurityUtil.isAuthenticated();
    }

    /**
     * Check if the current user can edit the active page
     * @return true if the current user can edit the active page.
     */
    public static boolean canEdit() {
        return Resource.getActivePage().isGranted(Permission.SET);
    }

    /**
     * Check if the current page is open in editing mode. Shortcut for checking if the server is admin, preview unset,
     * permissions to modify the page available for the current user.
     * @return true if the page is open in edit mode and user has permissions to edit
     */
    public static boolean isEditMode() {
        Content activePage = Resource.getActivePage();
        return Server.isAdmin()
            && !Resource.showPreview()
            && activePage != null
            && activePage.isGranted(Permission.SET);
    }

}
