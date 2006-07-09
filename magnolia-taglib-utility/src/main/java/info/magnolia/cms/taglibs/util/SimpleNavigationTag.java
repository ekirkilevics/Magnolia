/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.Resource;

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
 * </ul>
 * 
 * <pre>
 *   &lt;cmsu:simpleNavigation startLevel="3" style="mystyle"/>
 * </pre>
 * 
 * Will output the following:
 * 
 * <pre>
 *   &lt;ul class="level3 mystyle">
 *     &lt;li>&lt;a href="...">page 1 name &lt;/a>&lt;/li>
 *     &lt;li>&lt;a href="...">page 2 name &lt;/a>&lt;/li>
 *     &lt;li class="trail">&lt;a href="...">page 3 name &lt;/a>
 *       &lt;ul class="level4">
 *         &lt;li>&lt;a href="...">subpage 1 name &lt;/a>&lt;/li>
 *         &lt;li>&lt;a href="...">subpage 2 name &lt;/a>&lt;/li>
 *         &lt;li>&lt;strong>&lt;a href="...">selected page name &lt;/a>&lt;/strong>&lt;/li>
 *       &lt;/ul>
 *     &lt;/li>
 *     &lt;li>&lt;a href="...">page 4 name &lt;/a>&lt;/li>
 *   &lt;/ul>
 * </pre>
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
     * Expand all expand all the nodes
     */
    public static final String EXPAND_ALL = "all";

    /**
     * Expand all expand only page that should be displayed in navigation
     */
    public static final String EXPAND_SHOW = "show";

    /**
     * Do not use expand functions
     */
    public static final String EXPAND_NONE = "none";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleNavigationTag.class);

    /**
     * Start level.
     */
    private int startLevel;

    /**
     * End level
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
     * Style to apply to the menu
     */
    private String style;

    /**
     * Expand all the nodes (sitemap mode)
     */
    private String expandAll = EXPAND_NONE;

    /**
     * Setter for the <code>startLevel</code> tag attribute.
     * @param startLevel the start level for navigation, defaults to <code>0</code>.
     */
    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    /**
     * Setter for the <code>endLevel</code> tag attribute.
     * @param endLevel the end level for navigation, defaults to not used if not set
     */
    public void setEndLevel(int endLevel) {
        this.endLevel = endLevel;
    }

    /**
     * Setter for the <code>style</code> tag attribute.
     * @param style to apply to this menu, default is empty and not used
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Setter for <code>hideInNav</code> tag attribute.
     * @param hideInNav name for the "hide in nav" nodeData.
     */
    public void setHideInNav(String hideInNav) {
        this.hideInNav = hideInNav;
    }

    /**
     * Setter for <code>openMenu</code> tag attribute.
     * @param openMenu name for the "open menu" nodeData.
     */
    public void setOpenMenu(String openMenu) {
        this.openMenu = openMenu;
    }

    /**
     * @param expandAll The expandAll to set. If the value is different than <code>EXPAND_SHOW</code> then the call
     * expandAll is set to <code>EXPAND_ALL</code>
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
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        Content activePage = Resource.getCurrentActivePage((HttpServletRequest) this.pageContext.getRequest());
        JspWriter out = this.pageContext.getOut();

        if (startLevel > endLevel) {
            endLevel = 0;
        }

        try {
            if (this.startLevel <= activePage.getLevel()) {
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

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        this.startLevel = 0;
        this.endLevel = 0;
        this.hideInNav = null;
        this.openMenu = null;
        this.style = null;
        this.expandAll = EXPAND_NONE;
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

        Collection children = page.getChildren(ItemType.CONTENT);

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

        Iterator it = children.iterator();
        while (it.hasNext()) {
            Content child = (Content) it.next();

            if (expandAll.equalsIgnoreCase(EXPAND_NONE) || expandAll.equalsIgnoreCase(EXPAND_SHOW)) {
                if (child
                    .getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA))
                    .getBoolean()) {
                    continue;
                }
            }

            List cssClasses = new ArrayList(3);

            String title = child.getNodeData(NODEDATA_NAVIGATIONTITLE).getString(StringUtils.EMPTY);

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

            StringBuffer css = new StringBuffer(cssClasses.size() * 10);
            Iterator iterator = cssClasses.iterator();
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
            out.print(child.getHandle());
            out.print(".html\""); //$NON-NLS-1$

            if (StringUtils.isNotEmpty(accesskey)) {
                out.print(" accesskey=\""); //$NON-NLS-1$
                out.print(accesskey);
                out.print("\""); //$NON-NLS-1$
            }

            out.print(">"); //$NON-NLS-1$

            out.print(StringEscapeUtils.escapeHtml(title));
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
        Iterator it = page.getChildren().iterator();
        if (it.hasNext() && expandAll.equalsIgnoreCase(EXPAND_ALL)) {
            return true;
        }
        while (it.hasNext()) {
            Content ch = (Content) it.next();
            if (!ch.getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA)).getBoolean()) {
                return true;
            }
        }
        return false;
    }

}
