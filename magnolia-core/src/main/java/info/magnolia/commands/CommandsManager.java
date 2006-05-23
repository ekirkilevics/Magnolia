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
package info.magnolia.commands;

import javax.jcr.RepositoryException;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;


/**
 * Manages the Commands and Catalogs.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class CommandsManager extends ObservedManager {

    private static CommandsManager instance = (CommandsManager) FactoryUtil.getSingleton(CommandsManager.class);

    public static final String DEFAULT_CATALOG = "default";

    public static final String COMMAND_DELIM = "-";

    /**
     * Register this catalogue
     */
    protected void onRegister(Content node) {
        Catalog catalog = new MgnlRepositoryCatalog(node);
        String name = getCatalogName(node);
        if(log.isDebugEnabled()){
            log.debug("registering catalog {}", name);
        }
        CatalogFactory.getInstance().addCatalog(name, catalog);
    }

    /**
     * Get the name to use for this catalog. Checks first the presence of a ctalogName property. If not found and the
     * nodes name is 'commands' the parents name is used. Otherwise the catalog name is the name of the node itself.
     * @param node
     * @return the name of this catalog
     */
    protected String getCatalogName(Content node) {
        try {
            if (node.hasNodeData("catalogName")) {
                return node.getNodeData("catalogName").toString();
            }
            else if (node.getName().equals("commands")) {
                return node.getParent().getName();
            }
            else {
                return node.getName();
            }
        }
        catch (RepositoryException e) {
            return node.getName();
        }
    }

    /**
     * Clear all catalogues
     */
    protected void onClear() {
        CatalogFactory.clear();
    }

    /**
     * Get the command
     * @param catalogName the catalog containing the command
     * @param commandName the name of the command
     * @return the command to execute
     */
    public Command getCommand(String catalogName, String commandName) {
        Catalog catalog = CatalogFactory.getInstance().getCatalog(catalogName);
        if (catalog != null) {
            return catalog.getCommand(commandName);
        }
        else {
            return null;
        }
    }

    /**
     * Use a delimiter to separate the catalog and command name
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
        return instance;
    }

}
