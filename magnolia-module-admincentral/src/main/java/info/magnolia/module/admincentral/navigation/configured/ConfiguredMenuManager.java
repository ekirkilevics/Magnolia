/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.navigation.configured;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.admincentral.navigation.ConfiguredMenuProvider;
import info.magnolia.module.admincentral.navigation.MenuRegistry;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ObservedManager for menu items configured in repository.
 * @deprecated decide whether we need this or not
 */
public class ConfiguredMenuManager extends ObservedManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<String> registeredMenuItems = new HashSet<String>();

    @Override
    protected void onRegister(Content node) {
        Collection<Content> children = node.getChildren(ItemType.CONTENTNODE);

        for (Content menuNode : children) {
            String name = menuNode.getNodeData("name").getString();
            if (StringUtils.isEmpty(name)) {
                name = menuNode.getName();
            }

            synchronized (registeredMenuItems) {
                try {
                    ConfiguredMenuProvider menuProvider = new ConfiguredMenuProvider(menuNode);
                    MenuRegistry.getInstance().registerMenu(name, menuProvider);
                    this.registeredMenuItems.add(name);
                    log.info("registering menu {}", name);
                } catch (IllegalStateException e) {
                    log.error("Unable to register menu [" + name + "]", e);
                }
            }
        }
    }

    @Override
    protected void onClear() {
        synchronized (registeredMenuItems) {
            for (String menuName : registeredMenuItems) {
                MenuRegistry.getInstance().unregisterMenu(menuName);
            }
            this.registeredMenuItems.clear();
        }
    }

    public static ConfiguredMenuManager getInstance() {
        return Components.getSingleton(ConfiguredMenuManager.class);
    }
}
