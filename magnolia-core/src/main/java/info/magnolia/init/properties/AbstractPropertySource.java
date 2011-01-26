/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.init.properties;

import info.magnolia.init.PropertySource;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Abstract implementation of {@link PropertySource}, providing the basic mechanisms
 * for implementation based on an instance of {@link Properties}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractPropertySource implements PropertySource {
    // can't use a Map<String, String>, as java.util.Properties is a Map<Object, Object>.
    protected final Properties properties;

    public AbstractPropertySource(Properties properties) {
        this.properties = properties;
    }

    public Set<String> getKeys() {
        // TODO hum ...
        final HashSet<String> keys = new HashSet<String>();
        for (Object o : properties.keySet()) {
            keys.add((String) o);
        }
        return keys;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String describe() {
        return "[" + getClass().getSimpleName() + "]";
    }

    @Override
    public String toString() {
        return describe() + " with properties: " + properties.toString();
    }
}
