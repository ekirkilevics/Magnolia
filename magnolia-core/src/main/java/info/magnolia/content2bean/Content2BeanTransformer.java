/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;
import info.magnolia.objectfactory.ObjectFactory;

import java.util.Collection;
import java.util.Map;


/**
 * Used to create beans
 * @author philipp
 * @version $Id$
 */
public interface Content2BeanTransformer {
    /**
     * Create a state object to share the state between the processor and transformer
     */
    public TransformationState newState();

    /**
     * Resolves the class to use for the current node
     */
    public TypeDescriptor resolveType(TransformationState state) throws ClassNotFoundException;

    /**
     * Returns the children of the node to be transformed. Those are normally the direct children but might differ
     */
    public Collection<Content> getChildren(Content node);

    /**
     * Instantiates the bean
     */
    public Object newBeanInstance(TransformationState state, Map values) throws Content2BeanException;

    /**
     * Called after all properties are set
     */
    public void initBean(TransformationState state, Map values) throws Content2BeanException;

    /**
     * Set this property on that bean. Allows excluding of properties
     */
    public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values);

    /**
     * Transforms the simple basic jcr property value objects to more complex properties
     */
    public Object convertPropertyValue(Class<?> propertyType, Object value) throws Content2BeanException;

    /**
     * The mapping to use
     */
    public TypeMapping getTypeMapping();

    /**
     * Get your instance here
     */
    class Factory {
        public static Content2BeanTransformer getDefaultTransformer() {
            return ObjectFactory.components().getSingleton(Content2BeanTransformer.class);
        }
    }


}
