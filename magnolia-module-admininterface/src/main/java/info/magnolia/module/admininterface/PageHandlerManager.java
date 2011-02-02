/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.objectfactory.Components;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;


/**
 * Manages the page handlers. A page is a very simple dialog without any configuration.
 * @author philipp
 */
public class PageHandlerManager extends ObservedManager {

    /**
     * The handlers
     */
    private final Map dialogPageHandlers = new HashMap();

    /**
     * Find a handler by name
     * @param name
     * @param request
     * @param response
     * @return an instance of the handlers
     */
    public PageMVCHandler getPageHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        PageDefinition pageDefinition = (PageDefinition) dialogPageHandlers.get(name);

        if (pageDefinition == null) {
            log.warn("Page definition not found: \"{}\"", name);
            return null;
        }
        return pageDefinition.newInstance(name, request, response);
    }

    /**
     * register the pages from the config
     * @param defNode
     */
    protected void onRegister(Content defNode) {
        // read the dialog configuration

        for (Iterator iter = ContentUtil.getAllChildren(defNode).iterator(); iter.hasNext();) {
            Content pageNode = (Content) iter.next();

            PageDefinition pd = new RepositoryPageDefinition(new SystemContentWrapper(pageNode));
            registerPageDefinition(pd);
        }

    }

    public void registerPageDefinition(PageDefinition pageDefinition) {
        dialogPageHandlers.put(pageDefinition.getName(), pageDefinition);
    }

    /**
     * @deprecated
     */
    public void registerPageDefinition(String name, PageDefinition pageDefinition) {
        dialogPageHandlers.put(name, pageDefinition);
    }

    /**
     * @return Returns the instance.
     */
    public static PageHandlerManager getInstance() {
        return Components.getSingleton(PageHandlerManager.class);
    }

    protected void onClear() {
        this.dialogPageHandlers.clear();
    }

    public static interface PageDefinition {

        public String getName();

        public PageMVCHandler newInstance(String name, HttpServletRequest request, HttpServletResponse response);
    }

    /**
     * This class is used if you want to register a page that is not stored in the repository.
     * @author philipp
     * @version $Id$
     */
    public static class BasePageDefinition implements PageDefinition {
        private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasePageDefinition.class);

        private Map defaultProperties = new HashMap();

        private Class handlerClass;

        private String name;

        public BasePageDefinition(String name, Class handlerClass) {
            this.name = name;
            this.handlerClass = handlerClass;
        }

        public Map getDefaultProperties() {
            return this.defaultProperties;
        }

        public void setDefaultProperties(Map defaultProperties) {
            this.defaultProperties = defaultProperties;
        }

        public Class getHandlerClass() {
            return this.handlerClass;
        }

        public void setHandlerClass(Class handlerClass) {
            this.handlerClass = handlerClass;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PageMVCHandler newInstance(String name, HttpServletRequest request, HttpServletResponse response) {

            try {
                Constructor constructor = getHandlerClass().getConstructor(
                    new Class[]{String.class, HttpServletRequest.class, HttpServletResponse.class});
                PageMVCHandler page = (PageMVCHandler) constructor.newInstance(new Object[]{name, request, response});
                BeanUtils.populate(page, getDefaultProperties());
                return page;
            }
            catch (Exception e) {
                log.error("Can't instantiate page [" + name + "]", e);
                throw new InvalidDialogPageHandlerException(name, e);
            }

        }
    }

    public static class RepositoryPageDefinition implements PageDefinition {

        private Content node;

        public RepositoryPageDefinition(Content node) {
            this.node = node;
        }

        public String getName() {
            return NodeDataUtil.getString(this.node, "name", this.node.getName());
        }

        public PageMVCHandler newInstance(String name, final HttpServletRequest request,
            final HttpServletResponse response) {
            try {
                return (PageMVCHandler) Content2BeanUtil.toBean(node, true, new Content2BeanTransformerImpl() {

                    public Object newBeanInstance(TransformationState state, Map properties)
                        throws Content2BeanException {
                        if (state.getLevel() == 1) {
                            try {
                                // TODO - with ioc and a request-scope container, this can go away \o/
                                return ConstructorUtils.invokeConstructor(
                                    state.getCurrentType().getType(),
                                    new Object[]{getName(), request, response});
                            }
                            catch (Exception e) {
                                throw new Content2BeanException("no proper constructor found", e);
                            }
                        }

                        return super.newBeanInstance(state, properties);
                    }
                });
            }
            catch (Content2BeanException e) {
                throw new InvalidDialogPageHandlerException(this.getName(), e);
            }
        }
    }

}
