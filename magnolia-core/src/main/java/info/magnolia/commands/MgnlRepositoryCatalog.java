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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A command catalog using a repository node as a configuration. Chains are supported. Date: Mar 27, 2006 Time: 10:58:22
 * AM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 * @deprecated we use content2bean in CommandsManager
 */
public class MgnlRepositoryCatalog extends MgnlCatalog {

    private static Logger log = LoggerFactory.getLogger(MgnlRepositoryCatalog.class);

    static final String CLASS_NODE_DATA = "impl";

    /**
     * Initialize this catalog based on the configuration found in the repository
     */
    public MgnlRepositoryCatalog(Content content) {
        Iterator iter = content.getChildren(ItemType.CONTENTNODE).iterator();
        // loop over the command names one by one
        while (iter.hasNext()) {
            // get the action node and name
            Content actionNode = (Content) iter.next();
            String actionName = actionNode.getName();

            NodeData impl = actionNode.getNodeData(CLASS_NODE_DATA);
            if (impl != null && impl.getString() != null && !(impl.getString().equals(StringUtils.EMPTY))) {

                Command command;
                try {
                    command = createCommand(actionNode);
                    this.addCommand(actionName, command);
                }
                catch (Exception e) {
                    log.error("can't initialize command [" + actionName + "]", e);
                }
                // continue with next action
            }
            else {
                log.debug("This is a chain");

                Chain chain = new ChainBase();

                // consider any command as a chain, makes things easier
                // we iterate through each subnode and consider this as a command in the chain
                Collection childrens = actionNode.getChildren(ItemType.CONTENTNODE);

                log.debug("Found {}  children for action {}", Integer.toString(childrens.size()), actionName);
                Iterator iterNode = childrens.iterator();

                Exception e = null;
                while (iterNode.hasNext()) {
                    try {
                        Content commandNode = (Content) iterNode.next();
                        Command command = createCommand(commandNode);
                        chain.addCommand(command);
                    }
                    catch (Exception te) {
                        e = te;
                        break;
                    }
                }

                // add the command only if no error was reported
                if (e != null) {
                    log.error("Could not load commands for action:" + actionName, e);
                }
                else {
                    // add the chain to the catalog linked with the actionName
                    this.addCommand(actionName, chain);
                }
            }
        }
   }

    /**
     * Create a command object based on a node
     * @param commandNode
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws Content2BeanException
     */
    private Command createCommand(Content commandNode) throws InstantiationException,
        IllegalAccessException, Content2BeanException {
        Command command = null;
        String className;
        className = commandNode.getNodeData(CLASS_NODE_DATA).getString();

        log.debug("Found class {} for action {}", className, commandNode.getName());
        try{
            Class klass = ClassUtil.classForName(className);
            command = (Command) klass.newInstance();
            Content2BeanUtil.setProperties(command, commandNode);
            return command;
        }
        // asume it is a qualified command name
        catch(ClassNotFoundException e){
            // this will get the command at runtime from an other catalogue
            return new DelegateCommand(className);
        }
    }
}
