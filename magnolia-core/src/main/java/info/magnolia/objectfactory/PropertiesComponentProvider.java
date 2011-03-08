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
package info.magnolia.objectfactory;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link ComponentProvider} is using the configuration provided in a Properties object. Each property key is
 * the interface/base-class, and the value
 * is either the implementation-to-use class name, an implementation of {@link ComponentFactory}
 * which is used to instantiate the desired implementation, or the path to a node in the repository (in the form of
 * <code>repository:/path/to/node</code> or <code>/path/to/node</code>, which defaults to the <code>config</code>
 * repository). In the latter case, the component is constructed via {@link ObservedComponentFactory}
 * and reflects (through observation) the contents of the given path.
 *
 * @author Philipp Bracher
 * @version $Revision: 25238 $ ($Author: pbaerfuss $)
 */
public class PropertiesComponentProvider extends AbstractComponentProvider {

    private final static Logger log = LoggerFactory.getLogger(PropertiesComponentProvider.class);

    public PropertiesComponentProvider() {
    }

    public PropertiesComponentProvider(Properties mappings) {
        parseConfiguration(mappings);
    }

    public PropertiesComponentProvider(HierarchicalComponentProvider parent) {
        super(parent);
    }

    public void parseConfiguration(Properties mappings) {

        for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            final Class<?> type = classForName(key);
            if (type == null) {
                log.debug("{} does not seem to resolve to a class. (property value: {})", key, value);
                continue;
            }
            if (ComponentConfigurationPath.isComponentConfigurationPath(value)) {
                ComponentConfigurationPath path = new ComponentConfigurationPath(value);
                registerComponentFactory(type, new LazyObservedComponentFactory(path.getRepository(), path.getPath(), type));
            } else {
                Class<?> valueType = classForName(value);
                if (valueType == null) {
                    log.debug("{} does not seem to resolve a class or a configuration path. (property key: {})", value, key);
                } else {
                    registerComponent(type, valueType);
                }
            }
        }
    }

    protected Class<?> classForName(String value) {
        try {
            return Classes.getClassFactory().forName(value);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
