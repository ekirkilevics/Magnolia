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

import java.util.Iterator;
import java.util.Map;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import javax.jcr.RepositoryException;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the Commands and Catalogs.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class CommandsManager extends ObservedManager {

    public static final String DEFAULT_CATALOG = "default";

    public static final String COMMAND_DELIM = "-";

    protected static Logger log = LoggerFactory.getLogger(CommandsManager.class);

    /**
     * Register this catalogue
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
            MgnlCatalog catalog = (MgnlCatalog) Content2BeanUtil.toBean(node, true, new CatalogTransfomer());
            CatalogFactory.getInstance().addCatalog(catalog.getName(), catalog);

            if(log.isDebugEnabled()){
                log.debug("catalog {} registered: {}", new Object[]{catalog.getName(), catalog});
            }
        }
        catch (Content2BeanException e) {
            log.error("can't create catalog [" + node  + "]", e);
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

        return null;
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
        return (CommandsManager) FactoryUtil.getSingleton(CommandsManager.class);
    }


    protected static class CatalogTransfomer extends Content2BeanTransformerImpl {

        private static final String DEPRECATED_CATALOG_NAME_NODE_DATA = "catalogName";

        private static final String DEPRECATED_IMPL_NODE_DATA = "impl";

        protected TypeDescriptor onResolveClass(TransformationState state) {
            Class klass = null;
            // default class to use
            if(state.getLevel() == 1){
                klass = MgnlCatalog.class;
            }
            else{
                Content node = state.getCurrentContent();
                try {
                    if(node.hasNodeData(DEPRECATED_IMPL_NODE_DATA)){
                        log.warn("rename  '" + DEPRECATED_IMPL_NODE_DATA + "' to 'class' [" + node + "]!");
                        try {
                            klass = ClassUtil.classForName(node.getNodeData(DEPRECATED_IMPL_NODE_DATA).getString());
                        }
                        catch (ClassNotFoundException e) {
                            klass = DelegateCommand.class;
                        }
                    }
                    else{
                        klass = ChainBase.class;
                    }
                }
                catch (RepositoryException e) {
                    log.error("can't check " + DEPRECATED_IMPL_NODE_DATA + " nodedata [" + node + "]", e);
                }
            }
            if(klass != null){
                return this.getTypeMapping().getTypeDescriptor(klass);
            }
            return null;
        }

        public void initBean(TransformationState state, Map properties) throws Content2BeanException {
            // we add the commands here (reflection does not work)
            if(state.getCurrentBean() instanceof Catalog){
                Catalog catalog = (Catalog) state.getCurrentBean();
                for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
                    String name = (String) iter.next();
                    if(properties.get(name) instanceof Command){
                        Command command = (Command) properties.get(name);
                        catalog.addCommand(name, command);
                    }
                }
            }

            super.initBean(state, properties);
        }

        public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map values) {
            Object bean = state.getCurrentBean();
            if(bean instanceof MgnlCatalog){
                MgnlCatalog catalog = (MgnlCatalog) bean;
                if(values.containsKey(DEPRECATED_CATALOG_NAME_NODE_DATA)){
                    log.warn("rename the 'catalogName' nodedata to 'name' [" + state.getCurrentContent() + "]");
                    catalog.setName((String)values.get(DEPRECATED_CATALOG_NAME_NODE_DATA));
                }

                if (!values.containsKey("name") && state.getCurrentContent().getName().equals("commands")) {
                    try {
                        catalog.setName(state.getCurrentContent().getParent().getName());
                    }
                    catch (RepositoryException e) {
                        log.error("can't resolve catalog name by using parent node [" + state.getCurrentContent() + "]", e);
                    }
                }
            }

            // support old way (using impl) of configuring delegate commands
            if(bean instanceof DelegateCommand){
                DelegateCommand delegateCommand = (DelegateCommand) state.getCurrentBean();
                if(StringUtils.isEmpty(delegateCommand.getCommandName())){
                    log.warn("You should define the commandName property on [{}]", state.getCurrentContent());
                    delegateCommand.setCommandName((String) values.get(DEPRECATED_IMPL_NODE_DATA));
                }
            }

            super.setProperty(state, descriptor, values);
        }
    }

}
