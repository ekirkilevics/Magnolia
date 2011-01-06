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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.taglibs.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Draws a simple, css based, navigation menu. The menu layout can then be customized using css, and the default menu
 * should be enough for most uses. Two following page properties will also be used in the menu:
 * <ul>
 * <li><code>navTitle</code>: a title to use for the navigation menu, if different from the real page title</li>
 * <li><code>accessKey</code>: an optional access key which will be added to the link</li>
 * <li><code>wrappingElement</code>: an optional html element (div, span, p, etc) to go within the &lt;a&gt; tag wrapping the anchor text
 * </ul>
 *
 * @jsp.tag name="simpleNavigation" body-content="empty"
 * @jsp.tag-example
 * <pre>
 * &lt;cmsu:simpleNavigation startLevel="3" style="mystyle"/&gt;
 *
 * Will output the following:
 *
 * &lt;ul class="level3 mystyle"&gt;
 *     &lt;li&gt;&lt;a href="..."&gt;page 1 name &lt;/a&gt;&lt;/li&gt;
 *     &lt;li&gt;&lt;a href="..."&gt;page 2 name &lt;/a&gt;&lt;/li&gt;
 *     &lt;li class="trail"&gt;&lt;a href="..."&gt;page 3 name &lt;/a&gt;
 *         &lt;ul class="level3"&gt;
 *             &lt;li&gt;&lt;a href="..."&gt;subpage 1 name &lt;/a&gt;&lt;/li&gt;
 *             &lt;li&gt;&lt;a href="..."&gt;subpage 2 name &lt;/a&gt;&lt;/li&gt;
 *             &lt;li&gt;&lt;strong&gt;&lt;a href="..."&gt;selected page name &lt;/a&gt;&lt;/strong&gt;&lt;/li&gt;
 *         &lt;/ul&gt;
 *     &lt;/li&gt;
 *     &lt;li&gt;&lt;a href="..."&gt;page 4 name &lt;/a&gt;&lt;/li&gt;
 * &lt;/ul&gt;
 *</pre>
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SimpleNavigationTag extends TagSupport {

    /**
     * Css class added to active page.
     */
    private static final String CSS_LI_ACTIVE = "active";

    /**
     * Css class added to ancestor of the active page.
     */
    private static final String CSS_LI_TRAIL = "trail";

    /**
     * Css class added to leaf pages.
     */
    private static final String CSS_LI_LEAF = "leaf"; //$NON-NLS-1$

    /**
     * Css class added to open trees.
     */
    private static final String CSS_LI_CLOSED = "closed"; //$NON-NLS-1$

    /**
     * Css class added to closed trees.
     */
    private static final String CSS_LI_OPEN = "open"; //$NON-NLS-1$

    /**
     * Css class added to first li in ul.
     */
    private static final String CSS_LI_FIRST = "first"; //$NON-NLS-1$

    /**
     * Css class added to last li in ul.
     */
    private static final String CSS_LI_LAST = "last"; //$NON-NLS-1$

    /**
     * Page property: navigation title.
     */
    private static final String NODEDATA_NAVIGATIONTITLE = "navTitle"; //$NON-NLS-1$

    /**
     * Page property: access key.
     */
    public static final String NODEDATA_ACCESSKEY = "accessKey"; //$NON-NLS-1$

    /**
     * Default name for "open menu" nodeData.
     */
    public static final String DEFAULT_OPENMENU_NODEDATA = "openMenu"; //$NON-NLS-1$

    /**
     * Default name for "hide in nav" nodeData.
     */
    public static final String DEFAULT_HIDEINNAV_NODEDATA = "hideInNav"; //$NON-NLS-1$

    /**
     * Default name for "wrapperElement" nodeData.
     */
    public static final String DEFAULT_WRAPPERELEMENT_NODEDATA = ""; //$NON-NLS-1$

    /**
     * Expand all expand all the nodes.
     */
    public static final String EXPAND_ALL = "all";

    /**
     * Expand all expand only page that should be displayed in navigation.
     */
    public static final String EXPAND_SHOW = "show";

    /**
     * Do not use expand functions.
     */
    public static final String EXPAND_NONE = "none";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 224L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleNavigationTag.class);

    /**
     * Start level.
     */
    private int startLevel;

    /**
     * End level.
     */
    private int endLevel;

    /**
     * Name for the "hide in nav" nodeData.
     */
    private String hideInNav;

    /**
     * Name for the "open menu" nodeData.
     */
    private String openMenu;

    /**
     * Style to apply to the menu.
     */
    private String style;

    /**
     * html element to wrap the anchortext. (i.e. &lt;a&gt;&lt;wrapper&gt;...&lt;/wrapper&gt;&lt;/a&gt;)
     */
    public String wrapperElement;

    /**
     * Expand all the nodes. (sitemap mode)
     */
    private String expandAll = EXPAND_NONE;

    private boolean relativeLevels = false;

    /**
     * Name for a page property which will be written to the css class attribute.
     */
    private String classProperty;

    /**
     * Name for the "nofollow" hodeData (for link that must be ignored by search engines).
     */
    private String nofollow;


    /**
     * Content Filter to use to evaluate if a page should be drawn.
     */
    private ContentFilter filter;

    private String contentFilter = "";

    /**
     * Flag to set if the first and last li in each ul should be marked with a special css class.
     */
    private boolean markFirstAndLastElement = false;

    /**
     * The start level for navigation, defaults to 0.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    /**
     * The end level for navigation, defaults to 0.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setEndLevel(int endLevel) {
        this.endLevel = endLevel;
    }

    /**
     * The css class to be applied to the first ul. Default is empty.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Name for the "hide in nav" nodeData. If a page contains a boolean property with this name and
     * it is set to true, the page is not shown in navigation. Defaults to "hideInNav".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setHideInNav(String hideInNav) {
        this.hideInNav = hideInNav;
    }

    /**
     * Name for the "open menu" nodeData. If a page contains a boolean property with this name and
     * it is set to true, subpages are always shown also if the page is not selected.
     * Defaults to "openMenu".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setOpenMenu(String openMenu) {
        this.openMenu = openMenu;
    }

    /**
     * Name for the "nofollow" nodeData. If a page contains a boolean property with this name
     * and it is set to true, rel="nofollow" will be added to the generated link
     * (for links the should be ignored by search engines).
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setNofollow(String nofollow) {
        this.nofollow = nofollow;
    }

    /**
     * A variable in the pageContext that contains a content filter, determining if a given page should be drawn or not.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentFilter(String contentFilter) {
        this.contentFilter = contentFilter;
    }

    /**
     * Sitemap mode. Can be assigned the "show" value. Only showable pages will be displayed. Any other value will
     * result in displaying all pages.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setExpandAll(String expandAll) {
        if (expandAll.equalsIgnoreCase(EXPAND_SHOW)) {
            this.expandAll = expandAll;
        }
        else {
            this.expandAll = EXPAND_ALL;
        }
    }

    /**
     * If set to true, the startLevel and endLevel values are treated relatively to the current active page.
     * The default value is false.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setRelativeLevels(boolean relativeLevels) {
        this.relativeLevels = relativeLevels;
    }

    /**
     * Name for a page property that will hold a css class name which will be added to the html class attribute.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setClassProperty(String classProperty) {
        this.classProperty = classProperty;
    }

    /**
     * When specified, all links will have the anchortext wrapped in the supplied element. (such as "span")
     * @param wrapperElement name of an html element that will be included in the anchor, wrapping the anchortext
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setWrapperElement(String wrapperElement) {
        this.wrapperElement = wrapperElement;
    }

    /**
     * If set to true, a "first" or "last" css class will be added to the list of css classes of the
     * first and the last li in each ul.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setMarkFirstAndLastElement(boolean flag) {
        markFirstAndLastElement = flag;
    }

    public int doEndTag() throws JspException {
        Content activePage = Resource.getCurrentActivePage();
        try {
            while (!ItemType.CONTENT.getSystemName().equals(activePage.getNodeTypeName()) && activePage.getParent() != null) {
                activePage = activePage.getParent();
            }
        } catch (RepositoryException e) {
            log.error("Failed to obtain parent page for " + Resource.getCurrentActivePage().getHandle(), e);
            activePage = Resource.getCurrentActivePage();
        }
        JspWriter out = this.pageContext.getOut();

        if (StringUtils.isNotEmpty(this.contentFilter)) {
            try {
                filter = (ContentFilter) this.pageContext.getAttribute(this.contentFilter);
            }
            catch(ClassCastException e) {
                log.error("contentFilter assigned was not a content filter", e);
            }
        } else {
            filter = null;
        }

        if (startLevel > endLevel) {
            endLevel = 0;
        }

        try {
            final int activePageLevel = activePage.getLevel();
            // if we are to treat the start and end level as relative
            // to the active page, we adjust them here...
            if (relativeLevels) {
                this.startLevel += activePageLevel;
                this.endLevel += activePageLevel;
            }
            if (this.startLevel <= activePageLevel) {
                Content startContent = activePage.getAncestor(this.startLevel);
                drawChildren(startContent, activePage, out);
            }

        }
        catch (RepositoryException e) {
            log.error("RepositoryException caught while drawing navigation: " + e.getMessage(), e); //$NON-NLS-1$
            return EVAL_PAGE;
        }
        catch (IOException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }

        return EVAL_PAGE;
    }

    public void release() {
        this.startLevel = 0;
        this.endLevel = 0;
        this.hideInNav = null;
        this.openMenu = null;
        this.style = null;
        this.classProperty = null;
        this.expandAll = EXPAND_NONE;
        this.relativeLevels = false;
        this.wrapperElement = "";
        this.contentFilter = "";
        this.filter = null;
        this.nofollow = null;
        this.markFirstAndLastElement = false;
        super.release();
    }

    /**
     * Draws page children as an unordered list.
     * @param page current page
     * @param activePage active page
     * @param out jsp writer
     * @throws IOException jspwriter exception
     * @throws RepositoryException any exception thrown during repository reading
     */
    private void drawChildren(Content page, Content activePage, JspWriter out) throws IOException, RepositoryException {

        Collection<Content> children = new ArrayList<Content>(page.getChildren(ItemType.CONTENT));

        if (children.size() == 0) {
            return;
        }

        out.print("<ul class=\"level"); //$NON-NLS-1$
        out.print(page.getLevel());
        if (style != null && page.getLevel() == startLevel) {
            out.print(" ");
            out.print(style);
        }
        out.print("\">"); //$NON-NLS-1$

        Iterator<Content> iter = children.iterator();
        // loop through all children and discard those we don't want to display
        while(iter.hasNext()) {
            final Content child = iter.next();

            if (expandAll.equalsIgnoreCase(EXPAND_NONE) || expandAll.equalsIgnoreCase(EXPAND_SHOW)) {
                if (child
                    .getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA)).getBoolean()) {
                    iter.remove();
                    continue;
                }
                // use a filter
                if (filter != null) {
                    if (!filter.accept(child)) {
                        iter.remove();
                        continue;
                    }
                }
            } else {
                if (child.getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA)).getBoolean()) {
                    iter.remove();
                    continue;
                }
            }
        }

        boolean isFirst = true;
        Iterator<Content> visibleIt = children.iterator();
        while (visibleIt.hasNext()) {
            Content child = visibleIt.next();
            List<String> cssClasses = new ArrayList<String>(4);

            NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(child, NODEDATA_NAVIGATIONTITLE);
            String title = nodeData.getString(StringUtils.EMPTY);

            // if nav title is not set, the main title is taken
            if (StringUtils.isEmpty(title)) {
                title = child.getTitle();
            }

            // if main title is not set, the name of the page is taken
            if (StringUtils.isEmpty(title)) {
                title = child.getName();
            }

            boolean showChildren = false;
            boolean self = false;

            if (!expandAll.equalsIgnoreCase(EXPAND_NONE)) {
                showChildren = true;
            }

            if (activePage.getHandle().equals(child.getHandle())) {
                // self
                showChildren = true;
                self = true;
                cssClasses.add(CSS_LI_ACTIVE);
            }
            else if (!showChildren) {
                showChildren = (child.getLevel() <= activePage.getAncestors().size() && activePage.getAncestor(
                    child.getLevel()).getHandle().equals(child.getHandle()));
            }

            if (!showChildren) {
                showChildren = child
                    .getNodeData(StringUtils.defaultString(this.openMenu, DEFAULT_OPENMENU_NODEDATA))
                    .getBoolean();
            }

            if (endLevel > 0) {
                showChildren &= child.getLevel() < endLevel;
            }

            cssClasses.add(hasVisibleChildren(child) ? (showChildren ? CSS_LI_OPEN : CSS_LI_CLOSED) : CSS_LI_LEAF);

            if (child.getLevel() < activePage.getLevel()
                && activePage.getAncestor(child.getLevel()).getHandle().equals(child.getHandle())) {
                cssClasses.add(CSS_LI_TRAIL);
            }

            if (StringUtils.isNotEmpty(classProperty) && child.hasNodeData(classProperty)) {
                cssClasses.add(child.getNodeData(classProperty).getString(StringUtils.EMPTY));
            }

            if (markFirstAndLastElement && isFirst) {
                cssClasses.add(CSS_LI_FIRST);
                isFirst = false;
            }

            if (markFirstAndLastElement && !visibleIt.hasNext()) {
                cssClasses.add(CSS_LI_LAST);
            }

            StringBuffer css = new StringBuffer(cssClasses.size() * 10);
            Iterator<String> iterator = cssClasses.iterator();
            while (iterator.hasNext()) {
                css.append(iterator.next());
                if (iterator.hasNext()) {
                    css.append(" "); //$NON-NLS-1$
                }
            }

            out.print("<li class=\""); //$NON-NLS-1$
            out.print(css.toString());
            out.print("\">"); //$NON-NLS-1$

            if (self) {
                out.println("<strong>"); //$NON-NLS-1$
            }

            String accesskey = child.getNodeData(NODEDATA_ACCESSKEY).getString(StringUtils.EMPTY);

            out.print("<a href=\""); //$NON-NLS-1$
            out.print(((HttpServletRequest) this.pageContext.getRequest()).getContextPath());
            out.print(I18nContentSupportFactory.getI18nSupport().toI18NURI(child.getHandle()));
            out.print(".html\""); //$NON-NLS-1$

            if (StringUtils.isNotEmpty(accesskey)) {
                out.print(" accesskey=\""); //$NON-NLS-1$
                out.print(accesskey);
                out.print("\""); //$NON-NLS-1$
            }

            if (nofollow != null && child.getNodeData(this.nofollow).getBoolean())
            {
                out.print(" rel=\"nofollow\""); //$NON-NLS-1$
            }

            out.print(">"); //$NON-NLS-1$

            if (StringUtils.isNotEmpty(this.wrapperElement)) {
                out.print("<" + this.wrapperElement + ">"); //$NON-NLS-1$
            }

            out.print(StringEscapeUtils.escapeHtml(title));

            if (StringUtils.isNotEmpty(this.wrapperElement)) {
                out.print("</" + this.wrapperElement + ">"); //$NON-NLS-1$
            }

            out.print(" </a>"); //$NON-NLS-1$

            if (self) {
                out.println("</strong>"); //$NON-NLS-1$
            }

            if (showChildren) {
                drawChildren(child, activePage, out);
            }
            out.print("</li>"); //$NON-NLS-1$
        }

        out.print("</ul>"); //$NON-NLS-1$
    }

    /**
     * Checks if the page has a visible children. Pages with the <code>hide in nav</code> attribute set to
     * <code>true</code> are ignored.
     * @param page root page
     * @return <code>true</code> if the given page has at least one visible child.
     */
    private boolean hasVisibleChildren(Content page) {
        Collection<Content> children = page.getChildren();
        if (children.size() > 0 && expandAll.equalsIgnoreCase(EXPAND_ALL)) {
            return true;
        }
        for (Content ch : children) {
            if (!ch.getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA)).getBoolean()) {
                return true;
            }
        }
        return false;
    }

}
