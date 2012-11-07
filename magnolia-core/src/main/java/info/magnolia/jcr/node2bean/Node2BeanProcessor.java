/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.jcr.node2bean;

import info.magnolia.objectfactory.ComponentProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Transforms nodes to beans or maps. The transformer is use to resolve classes or to instantiate beans.
 */
public interface Node2BeanProcessor {

    /**
     * Transforms given node to bean.
     * @param node which will be transformed to bean
     * @return bean object
     * @throws Node2BeanException
     * @throws RepositoryException
     */
    public Object toBean(Node node) throws Node2BeanException, RepositoryException;

    /**
     * Transforms given node to bean. Class parameter will be used in transformer as default type.
     * @param node which will be transformed to bean
     * @param defaultClass default type
     * @return bean object
     * @throws Node2BeanException
     * @throws RepositoryException
     */
    public Object toBean(Node node, Class<?> defaultClass) throws Node2BeanException, RepositoryException;

    /**
     * Transforms the node to a bean using the passed transformer and component provider.
     * @param node which will be transformed to bean
     * @param recursive if set to true then all subnodes will be transformed as well
     * @return bean object
     * @throws Node2BeanException
     * @throws RepositoryException
     */
    public Object toBean(Node node, boolean recursive, final Node2BeanTransformer transformer, ComponentProvider componentProvider) throws Node2BeanException, RepositoryException;

    /**
     * Similar to <code>toBean()</code> but uses a passed bean as the root bean.
     * @todo better name - configureBean
     * @throws RepositoryException
     */
    public Object setProperties(final Object bean, Node node, boolean recursive, final Node2BeanTransformer transformer, ComponentProvider componentProvider) throws Node2BeanException, RepositoryException;

}
