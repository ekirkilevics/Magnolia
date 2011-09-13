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
package info.magnolia.test.mock;

import java.util.Map;
import java.util.Properties;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.ParameterResolver;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.LegacyComponentsConfigurer;


/**
 * This {@link info.magnolia.objectfactory.ComponentProvider} is using the configuration provided in a Properties
 * object. Each property key is the interface/base-class, and the value is either the implementation-to-use class name,
 * <code>repository:/path/to/node</code> or <code>/path/to/node</code>, which defaults to the <code>config</code>
 * an implementation of {@link info.magnolia.objectfactory.ComponentFactory} which is used to instantiate the desired
 * implementation, or the path to a node in the repository (in the form of repository). In the latter case, the
 * component is constructed via {@link info.magnolia.objectfactory.ObservedComponentFactory} and reflects (through
 * observation) the contents of the given path.
 *
 * @version $Id$
 */
public class PropertiesComponentProvider extends AbstractComponentProvider {

    public PropertiesComponentProvider() {
    }

    public PropertiesComponentProvider(Properties mappings) {
        parseConfiguration(mappings);
    }

    public PropertiesComponentProvider(ComponentProvider parent) {
        super(parent);
    }

    public void parseConfiguration(final Properties mappings) {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        new LegacyComponentsConfigurer() {
            @Override
            public void doWithConfiguration(ComponentProvider parentComponentProvider, ComponentProviderConfiguration configuration) {
                for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
                    addComponent(configuration, (String)entry.getKey(), (String)entry.getValue());
                }
            }
        }.doWithConfiguration(getParent(), configuration);
        configure(configuration);
    }

    @Override
    public <T> T newInstanceWithParameterResolvers(Class<T> type, ParameterResolver... parameters) {
        throw new UnsupportedOperationException();
    }
}
