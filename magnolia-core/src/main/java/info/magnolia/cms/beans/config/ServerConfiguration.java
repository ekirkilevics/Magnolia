/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.ObservedComponentFactory;

import java.util.Map;

/**
 * Holds the basic server configuration info.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
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

    public static ServerConfiguration getInstance() {
        return Components.getSingleton(ServerConfiguration.class);
    }

    public static final class InstanceFactory extends ObservedComponentFactory<ServerConfiguration> {
        public InstanceFactory() {
            super(ContentRepository.CONFIG, "/server", ServerConfiguration.class);
        }

        // the false parameter here is the important thing to keep (not recursive)
        protected ServerConfiguration transformNode(Content node) throws Content2BeanException {
            return (ServerConfiguration) Content2BeanUtil.toBean(node, false, new Content2BeanTransformerImpl() {
                public Object newBeanInstance(TransformationState state, Map properties) {
                    return new ServerConfiguration();
                }
            });
        }
    }
}
