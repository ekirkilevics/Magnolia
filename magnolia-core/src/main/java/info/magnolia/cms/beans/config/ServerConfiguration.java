/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.beans.config;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.ObservedComponentFactory;
import info.magnolia.repository.RepositoryConstants;

import java.util.Map;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Holds the basic server configuration info.
 */
@Singleton
public class ServerConfiguration {
    private String defaultExtension;
    private String defaultBaseUrl;
    private boolean admin;

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public void setDefaultExtension(String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public void setDefaultBaseUrl(String defaultBaseUrl) {
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * @deprecated since 4.5, use IoC
     */
    @Deprecated
    public static ServerConfiguration getInstance() {
        return Components.getSingleton(ServerConfiguration.class);
    }

    /**
     * A special instance factory preventing recursive instantiation. The server configuration contains sub-nodes but only the direct properties belong to the server configuration bean.
     */
    public static final class InstanceFactory extends ObservedComponentFactory<ServerConfiguration> {
        public InstanceFactory() {
            super(RepositoryConstants.CONFIG, "/server", ServerConfiguration.class);
        }

        // the false parameter here is the important thing to keep (not recursive)
        @Override
        protected ServerConfiguration transformNode(Node node) throws Node2BeanException, RepositoryException {
            return (ServerConfiguration) Components.getComponent(Node2BeanProcessor.class).toBean(node, false, new Node2BeanTransformerImpl() {
                @Override
                public Object newBeanInstance(TransformationState state, Map properties, ComponentProvider componentProvider) {
                    return new ServerConfiguration();
                }
            }, Components.getComponentProvider());
        }
    }
}
