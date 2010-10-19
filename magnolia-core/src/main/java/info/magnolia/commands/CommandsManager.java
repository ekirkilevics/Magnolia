/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.commands;

import java.util.Iterator;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.objectfactory.Components;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.apache.commons.lang.StringUtils;


/**
 * Manages the Commands and Catalogs.
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class CommandsManager extends ObservedManager {

    public static final String DEFAULT_CATALOG = "default";

    public static final String COMMAND_DELIM = "-";

    protected static Content2BeanTransformer COMMAND_TRANSFORMER = new CommandTransformer();

    /**
     * Register observation for command catalogs.
     */
    protected void onRegister(Content node) {
        // is this a catalog or a collection of catalogs?
        if(node.getChildren(ItemType.CONTENT).size() == 0){
            registerCatalog(node);
        }
        else{
            for (Iterator iter = node.getChildren(ItemType.CONTENT).iterator(); iter.hasNext();) {
                onRegister((Content) iter.next());
            }
        }
    }

    protected void registerCatalog(Content node) {
        try {
            MgnlCatalog catalog = (MgnlCatalog) Content2BeanUtil.toBean(node, true, COMMAND_TRANSFORMER);
            CatalogFactory factory = CatalogFactory.getInstance();
            if (factory.getCatalog(catalog.getName()) == null) {
                factory.addCatalog(catalog.getName(), catalog);
            } else {
                // runtime because this code is called by observation and there's no place to catch it anyway
                throw new RuntimeException("Catalog [" + catalog.getName() + "] is already registered. Please run: select * from nt:base where jcr:path like '/modules/%/commands/"+ catalog.getName()+"' on config repository to find out the duplicate.");
            }

            log.debug("Catalog {} registered: {}", new Object[]{catalog.getName(), catalog});
        }
        catch (Content2BeanException e) {
            log.error("Can't create catalog [" + node  + "]", e);
        }
    }

    /**
     * Clear all catalogs.
     */
    protected void onClear() {
        CatalogFactory.clear();
    }

    /**
     * Get the command.
     * @param catalogName the catalog containing the command
     * @param commandName the name of the command
     * @return the command to execute
     */
    public Command getCommand(String catalogName, String commandName) {
        Catalog catalog = CatalogFactory.getInstance().getCatalog(catalogName);
        if (catalog != null) {
            return catalog.getCommand(commandName);
        }

        return null;
    }

    /**
     * Use a delimiter to separate the catalog and command name.
     * @param commandName
     * @return the command
     */
    public Command getCommand(String commandName) {
        String catalogName = DEFAULT_CATALOG;
        if (StringUtils.contains(commandName, COMMAND_DELIM)) {
            catalogName = StringUtils.substringBefore(commandName, COMMAND_DELIM);
            commandName = StringUtils.substringAfter(commandName, COMMAND_DELIM);
        }

        Command command = getCommand(catalogName, commandName);
        if (command == null) {
            command = getCommand(DEFAULT_CATALOG, commandName);
        }
        return command;
    }

    /**
     * @return Returns the instance.
     */
    public static CommandsManager getInstance() {
        return Components.getSingleton(CommandsManager.class);
    }

}
