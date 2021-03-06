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
package info.magnolia.content2bean.impl;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanTransformer;

import java.util.Iterator;
import java.util.Properties;

import info.magnolia.objectfactory.ClassFactory;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Type mapper implementation resolving its mapping from the properties file. The transformer class is resolved from the value of the property named [fully_qualified_class_name].transformer.
 * @author philipp
 * @version $Id$
 *
 * @deprecated since 4.5 - register Transformers explicitly.
 */
public class PropertiesBasedTypeMapping extends TypeMappingImpl {

    private static Logger log = LoggerFactory.getLogger(PropertiesBasedTypeMapping.class);

    public PropertiesBasedTypeMapping() {
        Properties properties = SystemProperty.getProperties();
        for (Iterator<Object> iterator = properties.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if(key.endsWith(".transformer")){
                String className = StringUtils.removeEnd(key, ".transformer");
                String transformerClassName = properties.getProperty(key);
                try {
                    final ClassFactory cl = Classes.getClassFactory();
                    final Class<?> beanClass = cl.forName(className);
                    final Class<Content2BeanTransformer> transformerClass = cl.forName(transformerClassName);

                    // TODO - we can't instantiate the bastards here, unless we fetch them from IoC.

                    final Content2BeanTransformer transformer = cl.newInstance(transformerClass);

                    getTypeDescriptor(beanClass).setTransformer(transformer);
                    log.debug("Registered custom transformer [{}] for [{}]", className, transformerClassName);
                } catch (Exception e) {
                    log.error("Can't register custom transformer for [" + className + "]", e);
                }
            }
        }

    }
}
