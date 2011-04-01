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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;

import java.text.MessageFormat;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to create the menud
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class Navigation {

    private static final String CP_PREFIX = "contextPath + ";

    Logger log = LoggerFactory.getLogger(Navigation.class);

    /**
     * The node containing the menu and submenupoints
     */
    Content node;

    /**
     * The name of the Javascript variable
     */
    String jsName;

    /**
     * @param path the path to the menu
     */
    public Navigation(String path, String jsName) {
        try {
            // get it with system permission
            this.node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(path);
            this.jsName = jsName;
        }
        catch (Exception e) {
            log.error("can't initialize the menu", e);
        }
    }

    /**
     * Generate the code to initialize the js navigation
     * @return the javascript
     */
    public String getJavascript() {
        StringBuffer str = new StringBuffer();

        // name, id, text, link, icon
        String nodePattern = "{0}.addNode (\"{1}\", \"{2}\", \"{3}\", {4}\"{5}\");\n";
        // name, parentId, id, text, link, icon
        String subPattern = "{0}.getNode(\"{1}\").addNode (\"{2}\", \"{3}\", \"{4}\", {5}\"{6}\");\n";

        // loop over the menupoints
        for (Iterator iter = node.getChildren(ItemType.CONTENTNODE).iterator(); iter.hasNext();) {
            Content mp = (Content) iter.next();
            // check permission
            if (isMenuPointRendered(mp)) {
                String icon = NodeDataUtil.getString(mp, "icon");
                String contextPath = "";
                if (!"".equals(icon)) {
                    contextPath = CP_PREFIX;
                }
                str.append(MessageFormat.format(nodePattern, jsName,
                        mp.getUUID(),
                        getLabel(mp),
                        NodeDataUtil.getString(mp, "onclick"),
                        contextPath,
                        icon));


                // sub menupoints (2 level only)
                for (Iterator iterator = mp.getChildren(ItemType.CONTENTNODE).iterator(); iterator.hasNext();) {
                    Content sub = (Content) iterator.next();
                    if (isMenuPointRendered(sub)) {
                        String subIcon = NodeDataUtil.getString(sub, "icon");
                        String subContextPath = "";
                        if (!"".equals(subIcon)) {
                            subContextPath = CP_PREFIX;
                        }
                        str.append(MessageFormat.format(subPattern, jsName,
                                mp.getUUID(),
                                sub.getUUID(),
                                getLabel(sub),
                                NodeDataUtil.getString(sub, "onclick"),
                                subContextPath,
                                subIcon));

                    }
                }
            }
        }

        return str.toString();
    }

    /**
     * @param mp
     * @return
     */
    protected Object getLabel(Content mp) {
        return NodeDataUtil.getI18NString(mp, "label");
    }

    /**
     * @param mp
     * @return
     */
    protected boolean isMenuPointRendered(Content mp) {
        try {
            return MgnlContext.getJCRSession(ContentRepository.CONFIG).hasPermission(mp.getHandle(), Session.ACTION_READ);
        } catch (RepositoryException e) {
            log.debug("Failed to read navigation permission", e);
            return false;
        }
    }

    /**
     * Get the first onclick in this menu. Used as the default src in the content iframe
     * @return the href
     */
    public String getFirstId() {
        return getFirstId(node);
    }

    private String getFirstId(Content node) {
        for (Iterator iter = node.getChildren(ItemType.CONTENTNODE).iterator(); iter.hasNext();) {
            Content sub = (Content) iter.next();
            if (isMenuPointRendered(sub)) {
                if (StringUtils.isNotEmpty(NodeDataUtil.getString(sub, "onclick"))) {
                    return sub.getUUID();
                }
                String uuid = getFirstId(sub);
                if (StringUtils.isNotEmpty(uuid)) {
                    return uuid;
                }
            }
        }
        return "";
    }

}
