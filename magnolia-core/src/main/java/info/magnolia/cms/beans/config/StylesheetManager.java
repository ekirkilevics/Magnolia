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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author olli
 */
public class StylesheetManager extends ObservedManager {

    private Map stylesheets = new Hashtable();
    private static final String RESOURCE_PATH = "/.resources";

    private static final Logger logger = LoggerFactory.getLogger(StylesheetManager.class);

    public static StylesheetManager getInstance() {
        return (StylesheetManager) FactoryUtil.getSingleton(StylesheetManager.class);
    }

    public Map getStylesheets() {
        return stylesheets;
    }

    protected void onRegister(Content node) {
        registerStylesheets(node);
    }

    protected void onClear() {
        this.stylesheets.clear();
    }

    protected void registerStylesheets(Content node) {
        if (node != null) {
            logger.info("stylesheets node: " + node);
            Collection groupNodes = node.getChildren(ItemType.CONTENT);
            Iterator groupIterator = groupNodes.iterator();
            while (groupIterator.hasNext()) {
                Content groupNode = (Content) groupIterator.next();
                String groupName = groupNode.getName();
                Collection stylesheetNodes = groupNode.getChildren(ItemType.CONTENTNODE);
                Iterator stylesheetIterator = stylesheetNodes.iterator();
                while (stylesheetIterator.hasNext()) {
                    Content stylesheetNode = (Content) stylesheetIterator.next();
                    try {
                        Stylesheet stylesheet = (Stylesheet) Content2BeanUtil.toBean(stylesheetNode, Stylesheet.class);
                        stylesheet.setPath(StylesheetManager.RESOURCE_PATH + "/" + groupName + "/" + Stylesheet.TYPE + "/");
                        stylesheets.put(stylesheet.getName(), stylesheet);
                        logger.info("stylesheet {} added", stylesheet.getName());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            logger.info("stylesheets node is null");
        }
    }

}
