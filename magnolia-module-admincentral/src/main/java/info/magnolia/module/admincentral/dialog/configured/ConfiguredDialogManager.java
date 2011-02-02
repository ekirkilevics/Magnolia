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
package info.magnolia.module.admincentral.dialog.configured;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.admincentral.dialog.ConfiguredDialogProvider;
import info.magnolia.module.admincentral.dialog.DialogRegistry;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ObservedManager for dialogs configured in repository.
 */
public class ConfiguredDialogManager extends ObservedManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<String> registeredDialogs = new HashSet<String>();

    @Override
    protected void onRegister(Content node) {

        Collection<Content> children = node.getChildren(ItemType.CONTENTNODE);

        for (Content dialogNode : children) {

            String name = dialogNode.getNodeData("name").getString();
            if (StringUtils.isEmpty(name)) {
                name = dialogNode.getName();
            }

            synchronized (registeredDialogs) {
                try {
                    ConfiguredDialogProvider dialogProvider = new ConfiguredDialogProvider(dialogNode);
                    DialogRegistry.getInstance().registerDialog(name, dialogProvider);
                    this.registeredDialogs.add(name);
                } catch (IllegalStateException e) {
                    log.error("Unable to register dialog [" + name + "]", e);
                }
            }
        }
    }

    @Override
    protected void onClear() {
        synchronized (registeredDialogs) {
            for (String dialogName : registeredDialogs) {
                DialogRegistry.getInstance().unregisterDialog(dialogName);
            }
            this.registeredDialogs.clear();
        }
    }

    public static ConfiguredDialogManager getInstance() {
        return Components.getSingleton(ConfiguredDialogManager.class);
    }
}
