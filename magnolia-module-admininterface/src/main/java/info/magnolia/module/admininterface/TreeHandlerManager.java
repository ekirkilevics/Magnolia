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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.content2bean.Content2BeanUtil;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class TreeHandlerManager extends ObservedManager {

    private static final String ND_CLASS = "class";

    private static final String ND_REPOSITORY = "repository";

    private static final String ND_NAME = "name";

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map treeHandlers = new HashMap();

    /**
     * Get the tree handler registered under a particular name.
     * @param name
     * @param request
     * @param response
     * @return
     */
    public AdminTreeMVCHandler getTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        TreeHandlerConfig th = (TreeHandlerConfig) treeHandlers.get(name);

        if (th == null) {
            throw new InvalidTreeHandlerException(name);
        }

        Class treeHandlerClass = th.getHandler();

        try {
            Constructor constructor = treeHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            AdminTreeMVCHandler newInstance = (AdminTreeMVCHandler) constructor.newInstance(new Object[]{
                name,
                request,
                response});
            Content2BeanUtil.setProperties(newInstance, th.getTreeDefinition());
            return newInstance;
        }
        catch (Exception e) {
            throw new InvalidTreeHandlerException(name, e);
        }
    }

    protected void registerTreeHandler(String name, String repository, Class treeHandler, Content treeDefinition) {
        log.info("Registering tree handler {}", name); //$NON-NLS-1$
        treeHandlers.put(name, new TreeHandlerConfig(treeHandler, repository, treeDefinition));
    }

    protected void onRegister(Content defNode) {
        Collection trees = defNode.getChildren(ItemType.CONTENTNODE.getSystemName());
        for (Iterator iter = trees.iterator(); iter.hasNext();) {
            Content tree = (Content) iter.next();
            String name = tree.getNodeData(ND_NAME).getString();

            if (StringUtils.isEmpty(name)) {
                name = tree.getName();
            }

            String repository = tree.getNodeData(ND_REPOSITORY).getString();
            String className = tree.getNodeData(ND_CLASS).getString();

            if (StringUtils.isEmpty(repository)) {
                repository = name;
            }

            try {
                this.registerTreeHandler(name, repository, ClassUtil.classForName(className), new SystemContentWrapper(tree));
            }
            catch (ClassNotFoundException e) {
                log.error("Can't register tree handler [{}]: class [{}] not found", name, className);
            }

            // register commands if defined
            try {
                // TODO - this should go - maybe still needs an update task
                if (tree.hasContent("commands")) {
                    log.error("The definition of commands at the tree level is no longer supported. Move them to the modules commands node! [" + tree.getHandle() + "]");
                }
            }
            catch (RepositoryException e) {
                log.error("Can't check commands node of the tree node [" + tree.getHandle() + "]", e);
            }
        }
    }

    /**
     * @return Returns the instance.
     */
    public static TreeHandlerManager getInstance() {
        return (TreeHandlerManager) FactoryUtil.getSingleton(TreeHandlerManager.class);
    }

    /**
     * Clear the handlers
     */
    protected void onClear() {
        this.treeHandlers.clear();
    }

    class TreeHandlerConfig {

        private Class handler;

        private String repository;

        private Content treeDefinition;

        TreeHandlerConfig(Class handler, String repository, Content treeDefinition) {
            this.handler = handler;
            this.repository = repository;
            this.treeDefinition = treeDefinition;
        }

        public Class getHandler() {
            return this.handler;
        }

        public String getRepository() {
            return this.repository;
        }

        public Content getTreeDefinition() {
            return treeDefinition;
        }
    }

}
