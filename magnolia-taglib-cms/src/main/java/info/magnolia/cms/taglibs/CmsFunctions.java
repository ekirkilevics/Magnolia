package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.util.Properties;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Useful EL functions that can be used in jsp 2.0 pages.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class CmsFunctions {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CmsFunctions.class);

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

    /**
     * Find and load the first parent page containing a named collection of nodes. This function can be useful while
     * building pages that should inherit columns from parent pages. Loaded page must be unloaded using the
     * <code>&lt;cms:unloadPage /></code> tag. Sample use:
     *
     * <pre>
     * &lt;c:if test="${cmsfn:firstPageWithCollection("node", 3)}">
     *      content inherited from page ${cmsfn:currentPage().handle}.html
     *   &lt;cms:includeTemplate contentNodeName="node" />
     *   &lt;cms:unloadPage />
     * &lt;/c:if>
     * </pre>
     *
     * @param collectionName paragraph collection name
     * @param minlevel level at which we will stop also if no page is found
     * @return <code>true</code> if a page has been found and loaded, <code>false</code> otherwise
     */
    public static boolean firstPageWithCollection(String collectionName, int minlevel) {
        Content actpage = Resource.getCurrentActivePage();
        try {
            while (actpage.getLevel() > minlevel) {
                actpage = actpage.getParent();

                if (actpage.hasContent(collectionName) && actpage.getContent(collectionName).hasChildren()) {
                    Resource.setCurrentActivePage(actpage);
                    return true;
                }
            }
        }
        catch (RepositoryException e) {
            log.error("Error looking for collection " + collectionName + " in " + actpage.getHandle(), e);
        }

        return false;
    }

    /**
     * Find and load the first parent page containing a named node. This function can be useful while building pages
     * that should inherit a paragraph from parent pages. Loaded page must be unloaded using the
     * <code>&lt;cms:unloadPage /></code> tag. Sample use:
     *
     * <pre>
     * &lt;c:if test="${cmsfn:firstPageWithNode("column", 3)}">
     *      content inherited from page ${cmsfn:currentPage().handle}.html
     *   &lt;cms:contentNodeIterator contentNodeCollectionName="column">
     *     &lt;cms:includeTemplate />
     *   &lt;/cms:contentNodeIterator>
     *   &lt;cms:unloadPage />
     * &lt;/c:if>
     * </pre>
     *
     * @param nodeName paragraph name
     * @param minlevel level at which we will stop also if no page is found
     * @return <code>true</code> if a page has been found and loaded, <code>false</code> otherwise
     */
    public static boolean firstPageWithNode(String nodeName, int minlevel) {
        Content actpage = Resource.getCurrentActivePage();
        try {
            while (actpage.getLevel() > minlevel) {
                actpage = actpage.getParent();

                if (actpage.hasContent(nodeName)) {
                    Resource.setCurrentActivePage(actpage);
                    return true;
                }
            }
        }
        catch (RepositoryException e) {
            log.error("Error looking for node " + nodeName + " in " + actpage.getHandle(), e);
        }

        return false;
    }

}
