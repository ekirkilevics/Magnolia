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
package info.magnolia.commands;

import java.util.Iterator;
import java.util.Map;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import javax.jcr.RepositoryException;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Chain;
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
    private static final Logger log = LoggerFactory.getLogger(CommandsManager.class);

    public static final String DEFAULT_CATALOG = "default";

    public static final String COMMAND_DELIM = "-";

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
            MgnlCatalog catalog = (MgnlCatalog) Content2BeanUtil.toBean(node, true, COMMAND_TRANSFORMER);
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

    protected static Content2BeanTransformer COMMAND_TRANSFORMER = new Content2BeanTransformerImpl() {

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
                        // In case we are not yet building a concreate command we are creating a chain.
                        // Otherwise we are building command properties
                        boolean buildingCommand = false;
                        for (int i = 0; i < state.getLevel() -1; i++) {
                            TypeDescriptor td = state.peekType(i);
                            if(ClassUtil.isSubClass(td.getType(), Command.class) && !ClassUtil.isSubClass(td.getType(), Chain.class)){
                                buildingCommand = true;
                            }
                        }
                        if(!buildingCommand){
                            klass = ChainBase.class;
                        }
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

        public void initBean(TransformationState state, Map values) throws Content2BeanException {
            // we add the commands here (reflection does not work)
            if(state.getCurrentBean() instanceof Catalog){
                Catalog catalog = (Catalog) state.getCurrentBean();
                for (Iterator iter = values.keySet().iterator(); iter.hasNext();) {
                    String name = (String) iter.next();
                    if(values.get(name) instanceof Command){
                        Command command = (Command) values.get(name);
                        if(!(command instanceof MgnlCommand) || ((MgnlCommand)command).isEnabled()){
                            catalog.addCommand(name, command);
                        }
                    }
                }
            }

            // support chains
            if(state.getCurrentBean() instanceof Chain){
                Chain chain = (Chain) state.getCurrentBean();
                for (Iterator iter = values.keySet().iterator(); iter.hasNext();) {
                    String name = (String) iter.next();
                    if(values.get(name) instanceof Command){
                        Command command = (Command) values.get(name);
                        if(!(command instanceof MgnlCommand) || ((MgnlCommand)command).isEnabled()){
                            chain.addCommand(command);
                        }
                    }
                }
            }

            // support old way (using impl) of configuring delegate commands
            if(state.getCurrentBean() instanceof DelegateCommand){
                DelegateCommand delegateCommand = (DelegateCommand) state.getCurrentBean();
                if(StringUtils.isEmpty(delegateCommand.getCommandName())){
                    log.warn("You should define the commandName property on [{}]", state.getCurrentContent());
                    delegateCommand.setCommandName((String) values.get(DEPRECATED_IMPL_NODE_DATA));
                }
            }
            super.initBean(state, values);
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

            super.setProperty(state, descriptor, values);
        }
    };

}
