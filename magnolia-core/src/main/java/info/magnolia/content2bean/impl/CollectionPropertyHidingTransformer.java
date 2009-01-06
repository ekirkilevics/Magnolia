/**
 * This file Copyright (c) 2008-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.content2bean.impl;

import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A transformer which "hides" a collection node. Extend or pass the type and node name in the constructor.
 *
 * @author pbracher
 * @version $Revision$ ($Author$)
 */
public class CollectionPropertyHidingTransformer extends Content2BeanTransformerImpl {

    private static Logger log = LoggerFactory.getLogger(CollectionPropertyHidingTransformer.class);

    protected String collectionName;

    protected TypeDescriptor type;

    protected PropertyTypeDescriptor propertyDescriptor;

    protected Method addMethod;

    protected TypeDescriptor propertyType;

    public CollectionPropertyHidingTransformer(Class beanClass, String collectionName) {
        this.collectionName = collectionName;
        type =  getTypeMapping().getTypeDescriptor(beanClass);
        propertyDescriptor = type.getPropertyTypeDescriptor(collectionName);
        addMethod = propertyDescriptor.getAddMethod();
        propertyType = propertyDescriptor.getCollectionEntryType();
    }

    protected TypeDescriptor onResolveType(TransformationState state, TypeDescriptor resolvedType){
        if(resolvedType == null){
            // if we are transforming a child node which does not define
            // the class to be used, return the type of the collection entries

            // if the parent type is of the handled type
            // this is the case when we are transforming children nodes)
            if(state.getLevel()>1 && state.getCurrentType().equals(type)){
                // make it the default
                // use prop descr
                resolvedType = propertyType;
            }
        }
        return resolvedType;
    }

    public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map values) {
        if(descriptor.getName().equals(collectionName)){
            Object bean = state.getCurrentBean();

            for (Iterator iterator = values.entrySet().iterator(); iterator.hasNext();) {
                Entry entry = (Entry) iterator.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();

                if(propertyType.getType().isInstance(value) ){
                    try {
                        if(propertyDescriptor.isCollection()){
                            addMethod.invoke(bean, new Object[]{value});
                        }
                        else{
                            addMethod.invoke(bean, new Object[]{key, value});
                        }
                    } catch (Exception e) {
                        log.error("Can't call adder method " + propertyDescriptor.getAddMethod(), e);
                    }
                }
            }
        }
        else{
            super.setProperty(state, descriptor, values);
        }
    }
}