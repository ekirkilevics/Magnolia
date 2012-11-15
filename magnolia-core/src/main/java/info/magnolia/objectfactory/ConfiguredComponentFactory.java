/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.objectfactory;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.util.SessionUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.google.inject.ProvisionException;

/**
 * Builds a component configured in the repository by using node2bean.
 * 
 * @param <T> the components type
 * @version $Id$
 */
public class ConfiguredComponentFactory<T> implements ComponentFactory<T> {

    private final String path;
    private final String workspace;
    private final ComponentProvider componentProvider;

    public ConfiguredComponentFactory(String path, String workspace, ComponentProvider componentProvider) {
        this.path = path;
        this.workspace = workspace;
        this.componentProvider = componentProvider;
    }

    @Override
    public T newInstance() {
        final Node node = SessionUtil.getNode(workspace, path);
        if (node == null) {
            throw new NullPointerException("Can't find a the node [" + workspace + ":" + path + "] to create an instance");
        }
        try {
            return (T) Components.getComponent(Node2BeanProcessor.class).toBean(node, true, new Node2BeanTransformerImpl(), componentProvider);
        } catch (Node2BeanException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new ProvisionException("Can't read node [" + workspace + ":" + path + "]", e);
        }
    }
}
