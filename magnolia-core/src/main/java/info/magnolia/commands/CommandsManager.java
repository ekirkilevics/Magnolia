/**
 * This file Copyright (c) 2003-2013 Magnolia International
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

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.commands.chain.Catalog;
import info.magnolia.commands.chain.Command;
import info.magnolia.commands.chain.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * Manages the Commands and Catalogs.
 *
 */
@Singleton
public class CommandsManager extends ObservedManager {

    public static final String DEFAULT_CATALOG = "default";

    public static final String COMMAND_DELIM = "-";

    private final CommandTransformer commandTransformer;

    private final Node2BeanProcessor nodeToBean;

    private Map<String, MgnlCatalog> catalogs;

    @Inject
    public CommandsManager(Node2BeanProcessor nodeToBean) {
        this.nodeToBean = nodeToBean;
        this.commandTransformer = new CommandTransformer();
        this.catalogs = new HashMap<String, MgnlCatalog>();
    }

    /**
     * Register observation for command catalogs.
     */
    @Override
    protected void onRegister(Content node) {
        // is this a catalog or a collection of catalogs?
        if (node.getChildren(NodeTypes.Content.NAME).size() == 0) {
            registerCatalog(node);
        }
        else{
            for (Iterator iter = node.getChildren(NodeTypes.Content.NAME).iterator(); iter.hasNext();) {
                onRegister((Content) iter.next());
            }
        }
    }


    protected void registerCatalog(Content node) {
        log.info("Registering commands at {}...", node.getHandle());
        try {
            MgnlCatalog catalog = (MgnlCatalog) nodeToBean.toBean(node.getJCRNode(), true, commandTransformer, Components.getComponentProvider());
            MgnlCatalog current = catalogs.get(catalog.getName());
            if (current == null) {
                catalogs.put(catalog.getName(), catalog);
                log.info("Catalog [{}] registered: {}", catalog.getName(), catalog);
            } else {
                Iterator<String> names = catalog.getNames();
                while (names.hasNext()) {
                    String commandName = names.next();
                    Command command = current.getCommand(commandName);
                    if (command != null) {
                        log.warn(String.format("Command [%s] found at [%s] already exists in the catalog [%s], skipping...", commandName, node.getHandle(), current.getName()));
                    } else {
                        log.info("Adding new command [{}] to already registered catalog [{}]...", commandName, current.getName());
                        current.addCommand(commandName, catalog.getCommand(commandName));
                    }
                }
            }
        }
        catch (RepositoryException e) {
            log.error("Can't read catalog [" + node + "]", e);
        }
        catch (Node2BeanException e) {
            log.error("Can't create catalog [" + node  + "]", e);
        }
    }

    /**
     * Clear all catalogs.
     */
    @Override
    protected void onClear() {
        catalogs.clear();
    }

    /**
     * Get the command.
     * @param catalogName the catalog containing the command
     * @param commandName the name of the command
     * @return the command to execute
     */
    public Command getCommand(String catalogName, String commandName) {
        // if empty catalog name, use default catalog
        MgnlCatalog catalog = catalogs.get(StringUtils.isNotEmpty(catalogName) ? catalogName : DEFAULT_CATALOG);
        if (catalog != null) {
            Command command = catalog.getCommand(commandName);
            try {
                if (command != null) {
                    return command.getClass().newInstance();
                }
            } catch (IllegalAccessException iae) {
                log.warn("Cannot create new instance of command [" + commandName + "] from catalog [" + catalogName + "].", iae);
            } catch (InstantiationException ie) {
                log.warn("Cannot create new instance of command [" + commandName + "] from catalog [" + catalogName + "].", ie);
            }
        }

        return null;
    }

    /**
     * Use a delimiter to separate the catalog and command name.
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
     * @deprecated since 4.5, use IoC !
     */
    @Deprecated
    public static CommandsManager getInstance() {
        return Components.getComponent(CommandsManager.class);
    }

    /**
     * Executes the given command at the given catalog with the given parameters.
     * 
     * @throws Exception if an error occurs during command execution or if the command could not be found in any catalog.
     */
    public boolean executeCommand(final String catalogName, final String commandName, final Map<String, Object> params) throws Exception {
        final Command command = getCommand(catalogName, commandName);
        if (command == null) {
            throw new Exception(String.format("Command [%s] could not be found in catalog [%s]", commandName, catalogName));
        }
        log.debug("Executing command [{}] from catalog [{}] and params [{}]...", new Object[] { commandName, catalogName, params });
        return executeCommand(command, params);
    }

    /**
     * Executes the given command by first looking in the default catalog.
     * Should the command not be found, it will try to look in all other catalogs.
     * 
     * @see CommandsManager#executeCommand(String, String, Map)
     */
    public boolean executeCommand(final String commandName, final Map<String, Object> params) throws Exception {
        return executeCommand(DEFAULT_CATALOG, commandName, params);
    }

    /**
     * Executes the given command.
     * 
     * @see CommandsManager#executeCommand(String, Map)
     */
    public boolean executeCommand(final Command command, final Map<String, Object> params) throws Exception {

        Context context = MgnlContext.getInstance();
        if (params != null) {
            for (Entry<String, Object> param : params.entrySet()) {
                context.put(param.getKey(), param.getValue());
            }
        }
        return command.execute(context);
    }

    Catalog getCatalogByName(String name) {
        return catalogs.get(name);
    }

}
