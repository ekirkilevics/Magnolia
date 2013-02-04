/**
 * This file Copyright (c) 2010-2013 Magnolia International
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


import info.magnolia.commands.chain.Catalog;
import info.magnolia.commands.chain.Chain;
import info.magnolia.commands.chain.ChainBase;
import info.magnolia.commands.chain.Command;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentProvider;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
* Command to transform old "impl" reference to implementing class to new "class" node data name for references.
*/
public class CommandTransformer extends Node2BeanTransformerImpl {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommandTransformer.class);

    private static final String DEPRECATED_CATALOG_NAME_NODE_DATA = "catalogName";

    private static final String DEPRECATED_IMPL_NODE_DATA = "impl";

    @Override
    protected TypeDescriptor onResolveType(TypeMapping typeMapping, TransformationState state, TypeDescriptor resolvedType, ComponentProvider componentProvider) {
        if(resolvedType != null){
            return resolvedType;
        }
        Class klass = null;
        // default class to use
        if(state.getLevel() == 1){
            klass = MgnlCatalog.class;
        }
        else{
            Node node = state.getCurrentNode();
            try {
                if(node.hasProperty(DEPRECATED_IMPL_NODE_DATA)){
                    log.warn("Rename  '" + DEPRECATED_IMPL_NODE_DATA + "' to 'class' [" + node + "]!");
                    try {
                        final String className = node.getProperty(DEPRECATED_IMPL_NODE_DATA).getString();
                        klass = Classes.getClassFactory().forName(className);
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
                        if(isCommandClass(td.getType()) && !isChainClass(td.getType())){
                            buildingCommand = true;
                        }
                    }
                    if(!buildingCommand){
                        klass = ChainBase.class;
                    }
                }
            }
            catch (RepositoryException e) {
                log.error("Can't check " + DEPRECATED_IMPL_NODE_DATA + " nodedata [" + node + "]", e);
            }
        }
        if(klass != null){
            return typeMapping.getTypeDescriptor(klass);
        }
        return resolvedType;
    }

    @Override
    public void initBean(TransformationState state, Map values) throws Node2BeanException {
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
            for (Iterator iter = values.values().iterator(); iter.hasNext();) {
                Object value = iter.next();
                if (value instanceof Command) {
                    Command command = (Command) value;
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
                log.warn("You should define the commandName property on [{}]", state.getCurrentNode());
                delegateCommand.setCommandName((String) values.get(DEPRECATED_IMPL_NODE_DATA));
            }
        }
        super.initBean(state, values);
    }

    @Override
    public void setProperty(TypeMapping typeMapping, TransformationState state, PropertyTypeDescriptor descriptor, Map values) throws RepositoryException {
        Object bean = state.getCurrentBean();
        if(bean instanceof MgnlCatalog){
            MgnlCatalog catalog = (MgnlCatalog) bean;
            if(values.containsKey(DEPRECATED_CATALOG_NAME_NODE_DATA)){
                log.warn("Rename the 'catalogName' nodedata to 'name' [" + state.getCurrentNode() + "]");
                catalog.setName((String)values.get(DEPRECATED_CATALOG_NAME_NODE_DATA));
            }

            if (!values.containsKey("name") && state.getCurrentNode().getName().equals("commands")) {
                try {
                    catalog.setName(state.getCurrentNode().getParent().getName());
                }
                catch (RepositoryException e) {
                    log.error("Can't resolve catalog name by using parent node [" + state.getCurrentNode() + "]", e);
                }
            }
        }

        super.setProperty(typeMapping, state, descriptor, values);
    }

    protected boolean isCommandClass(Class<?> type) {
        return Command.class.isAssignableFrom(type);
    }

    protected boolean isChainClass(Class<?> type) {
        return Chain.class.isAssignableFrom(type);
    }
}
