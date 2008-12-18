/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Properties;


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
        return MgnlContext.getContextPath() + handle + '.' + ServerConfiguration.getInstance().getDefaultExtension();
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
        return MgnlContext.getAggregationState().getMainContent().isGranted(Permission.SET);
    }

    /**
     * Check if the current page is open in editing mode. Shortcut for checking if the server is admin, preview unset,
     * permissions to modify the page available for the current user.
     * @return true if the page is open in edit mode and user has permissions to edit
     */
    public static boolean isEditMode() {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        Content activePage = aggregationState.getMainContent();
        return ServerConfiguration.getInstance().isAdmin()
            && !aggregationState.isPreviewMode()
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

    /**
     * Function to iterate over a node Data that has "checkbox" as control type, for example.
     * See http://jira.magnolia.info/browse/MAGNOLIA-1969
     */
    public static Collection nodeDataIterator(Content c, String collection) {
        try {
            return c.getContent(collection).getNodeDataCollection();
        } catch (RepositoryException e) {
            log.error("Error when getting nodedata collection from " + c + " / " + collection + " :" + e.getMessage(), e);
            return null;
        }
    }

}
