/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentHandler;
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
import org.apache.log4j.Logger;


/**
 * Draws a simple, css based, navigation menu. The menu layout can then be customized using css, and the default menu
 * should be enough for most uses.
 * 
 * <pre>
 *   &lt;cmsu:simpleNavigation startLevel="3" />
 * </pre>
 * 
 * Will output the following:
 * 
 * <pre>
 *   &lt;ul class="level3">
 *     &lt;li>&lt;a href="...">page 1 name &lt;/a>&lt;/li>
 *     &lt;li>&lt;a href="...">page 2 name &lt;/a>&lt;/li>
 *     &lt;li>&lt;strong>&lt;a href="...">selected page name &lt;/a>&lt;/strong>
 *       &lt;ul class="level3">
 *         &lt;li>&lt;a href="...">subpage 1 name &lt;/a>&lt;/li>
 *         &lt;li>&lt;a href="...">subpage 2 name &lt;/a>&lt;/li>
 *         &lt;li>&lt;a href="...">subpage 3 name &lt;/a>&lt;/li>
 *       &lt;/ul>
 *     &lt;/li>
 *     &lt;li>&lt;a href="...">page 4 name &lt;/a>&lt;/li>
 *   &lt;/ul>
 * </pre>
 * 
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class SimpleNavigationTag extends TagSupport {

    /**
     * Default name for "open menu" nodeData.
     */
    public static final String DEFAULT_OPENMENU_NODEDATA = "openMenu";

    /**
     * Default name for "hide in nav" nodeData.
     */
    public static final String DEFAULT_HIDEINNAV_NODEDATA = "hideInNav";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SimpleNavigationTag.class);

    /**
     * Start level.
     */
    private int startLevel;

    /**
     * Name for the "hide in nav" nodeData.
     */
    private String hideInNav;

    /**
     * Name for the "open menu" nodeData.
     */
    private String openMenu;

    /**
     * Setter for the <code>startLevel</code> tag attribute.
     * @param startLevel the start level for navigation, defaults to <code>0</code>.
     */
    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
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
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        Content activePage = Resource.getActivePage((HttpServletRequest) this.pageContext.getRequest());
        JspWriter out = this.pageContext.getOut();
        try {
            drawChildren(activePage.getAncestor(this.startLevel), activePage, out);
        }
        catch (RepositoryException e) {
            log.error("RepositoryException caught while drawing navigation: " + e.getMessage(), e);
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
        this.hideInNav = null;
        this.openMenu = null;
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

        Collection children = page.getChildren(ItemType.CONTENT, ContentHandler.SORT_BY_SEQUENCE);

        if (children.size() == 0) {
            return;
        }

        out.print("<ul class=\"level");
        out.print(page.getLevel());
        out.print("\">");

        Iterator it = children.iterator();
        while (it.hasNext()) {
            Content child = (Content) it.next();

            if (child.getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA)).getBoolean()) {
                continue;
            }

            List cssClasses = new ArrayList(3);

            String title = child.getNodeData("navTitle").getString(StringUtils.EMPTY);

            // if nav title is not set, the main title is taken
            if (StringUtils.isEmpty(title)) {
                title = child.getTitle();
            }

            // if main title is not set, the name of the page is taken
            if (StringUtils.isEmpty(title)) {
                title = child.getName();
            }

            boolean showChildren;
            boolean self = false;

            if (activePage.getHandle().equals(child.getHandle())) {
                // self
                showChildren = true;
                self = true;
                cssClasses.add("active");
            }
            else {
                showChildren = (child.getLevel() <= activePage.getAncestors().size() && activePage.getAncestor(
                    child.getLevel()).getHandle().equals(child.getHandle()));
            }

            if (!showChildren) {
                showChildren = child
                    .getNodeData(StringUtils.defaultString(this.openMenu, DEFAULT_OPENMENU_NODEDATA))
                    .getBoolean();
            }

            cssClasses.add(hasVisibleChildren(child) ? (showChildren ? "open" : "closed") : "leaf");

            StringBuffer css = new StringBuffer(cssClasses.size() * 10);
            Iterator iterator = cssClasses.iterator();
            while (iterator.hasNext()) {
                css.append(iterator.next());
                if (iterator.hasNext()) {
                    css.append(" ");
                }
            }

            out.print("<li class=\"");
            out.print(css.toString());
            out.print("\">");

            if (self) {
                out.println("<strong>");
            }

            out.print("<a href=\"");
            out.print(((HttpServletRequest) this.pageContext.getRequest()).getContextPath());
            out.print(child.getHandle());
            out.print(".html\">");
            out.print(StringEscapeUtils.escapeHtml(title));
            out.print(" </a>");

            if (self) {
                out.println("</strong>");
            }

            if (showChildren) {
                drawChildren(child, activePage, out);
            }
            out.print("</li>");
        }

        out.print("</ul>");
    }

    /**
     * Checks if the page has a visible children. Pages with the <code>hide in nav</code> attribute set to
     * <code>true</code> are ignored.
     * @param page root page
     * @return <code>true</code> if the given page has at least one visible child.
     */
    private boolean hasVisibleChildren(Content page) {
        Iterator it = page.getChildren().iterator();
        while (it.hasNext()) {
            Content ch = (Content) it.next();
            if (!ch.getNodeData(StringUtils.defaultString(this.hideInNav, DEFAULT_HIDEINNAV_NODEDATA)).getBoolean()) {
                return true;
            }
        }
        return false;
    }

}
