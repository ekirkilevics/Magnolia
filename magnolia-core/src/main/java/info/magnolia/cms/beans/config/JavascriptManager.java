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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author olli
 */
public class JavascriptManager extends ObservedManager {

    private Map javascripts = new Hashtable();
    private static final String RESOURCE_PATH = "/.resources";

    private static final Logger logger = LoggerFactory.getLogger(JavascriptManager.class);

    public static JavascriptManager getInstance() {
        return (JavascriptManager) FactoryUtil.getSingleton(JavascriptManager.class);
    }

    public Map getJavascripts() {
        return javascripts;
    }

    protected void onRegister(Content node) {
        registerJavascripts(node);
    }

    protected void onClear() {
        this.javascripts.clear();
    }

    protected void registerJavascripts(Content node) {
        if (node != null) {
            logger.info("javascripts node: " + node);
            Collection groupNodes = node.getChildren(ItemType.CONTENT);
            Iterator groupIterator = groupNodes.iterator();
            while (groupIterator.hasNext()) {
                Content groupNode = (Content) groupIterator.next();
                String groupName = groupNode.getName();
                Collection javascriptNodes = groupNode.getChildren(ItemType.CONTENTNODE);
                Iterator javascriptIterator = javascriptNodes.iterator();
                while (javascriptIterator.hasNext()) {
                    Content javascriptNode = (Content) javascriptIterator.next();
                    try {
                        Javascript javascript = (Javascript) Content2BeanUtil.toBean(javascriptNode, Javascript.class);
                        javascript.setPath(JavascriptManager.RESOURCE_PATH + "/" + groupName + "/" + Javascript.TYPE + "/");
                        javascripts.put(javascript.getName(), javascript);
                        logger.info("javascript {} added", javascript.getName());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            logger.info("javascripts node is null");
        }
    }

    public List getOrderedJavascripts() {
        final List list = new ArrayList(javascripts.values());
        Collections.sort(list, new JavascriptDependencyLevelComparator(javascripts));
        return list;
    }

    /**
     * taken from:
     * info.magnolia.module.model.reader.DependencyLevelComparator
     */
    class JavascriptDependencyLevelComparator implements Comparator {

        private final Map javascripts;

        JavascriptDependencyLevelComparator(Map javascripts) {
            this.javascripts = javascripts;
        }

        public int compare(Object arg1, Object arg2) {
            final Javascript javascript1 = (Javascript) arg1;
            final Javascript javascript2 = (Javascript) arg2;

            int level1 = calcDependencyLevel(javascript1);
            int level2 = calcDependencyLevel(javascript2);

            // lower level first
            int diff = level1 - level2;
            if (diff != 0) {
                return diff;
            } else {
                // rest is ordered alphabetically
                return javascript1.getName().compareTo(javascript2.getName());
            }
        }

        /**
         * Calculates the level of dependency. 0 means no dependency. If no of the dependencies has itself dependencies is
         * this level 1. If one or more of the dependencies has a dependencies has a dependency it would return 2. And so on
         * ...
         *
         * @param javascript
         * @return the level
         */
        protected int calcDependencyLevel(Javascript javascript) {
            if (javascript.getDependencies() == null || javascript.getDependencies().size() == 0) {
                return 0;
            } else {
                final List dependencyLevels = new ArrayList();
                Iterator iterator = javascript.getDependencies().iterator();
                while (iterator.hasNext()) {
                    final String dependencyName = (String) iterator.next();
                    final Javascript dependency = (Javascript) javascripts.get(dependencyName);
                    if (dependency == null) {
                        throw new RuntimeException("missing dependency " + dependency.getName() + " for javascript " + javascript.getName());
                    } else {
                        dependencyLevels.add(new Integer(calcDependencyLevel(dependency)));
                    }
                }
                return ((Integer) Collections.max(dependencyLevels)).intValue() + 1;
            }
        }

    }

}
