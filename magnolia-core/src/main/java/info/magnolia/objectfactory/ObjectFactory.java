/**
 * This file Copyright (c) 2010 Magnolia International
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

import info.magnolia.cms.core.SystemProperty;
import org.apache.commons.lang.StringUtils;

/**
 * The ObjectFactory is the central point for accessing and instantiating objects configured in Magnolia.
 * It uses the system properties to determine which implementations to use.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ObjectFactory {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ObjectFactory.class);

    private static final ComponentProvider componentProvider = new DefaultComponentProvider(SystemProperty.getProperties());

    public static ClassFactory classes() {
        final String classFactoryClassName = SystemProperty.getProperty(ClassFactory.class.getName());

        if (StringUtils.isEmpty(classFactoryClassName)) {
            // use a DefaultClassFactory until the property is set
            return new DefaultClassFactory();
        } else {
            // whichever ClassFactory is registered will be instantiated with DefaultClassFactory.
            final DefaultClassFactory temp = new DefaultClassFactory();
            try {
                final Class<ClassFactory> c = temp.forName(classFactoryClassName);
                // TODO - cache !
                return temp.newInstance(c);
            } catch (ClassNotFoundException e) {
                log.error("Could not find {}, will use DefaultClassFactory for now");
                return new DefaultClassFactory();
            }
        }
    }

    public static ComponentProvider components() {
        return componentProvider;
    }

}
