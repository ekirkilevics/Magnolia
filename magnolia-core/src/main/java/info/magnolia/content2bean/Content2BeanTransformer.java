/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;

import java.util.Map;


/**
 * Used to create beans
 * @author philipp
 * @version $Id$
 */
public interface Content2BeanTransformer extends Content.ContentFilter {
    /**
     * Create a state object to share the state between the processor and transformer
     */
    public TransformationState newState();

    /**
     * Resolves the class to use for the current node
     */
    public TypeDescriptor resolveType(TransformationState state) throws ClassNotFoundException;

    /**
     * Instantiates the bean
     */
    public Object newBeanInstance(TransformationState state, Map values);

    /**
     * Called after all properties are set
     */
    public void initBean(TransformationState state, Map values) throws Content2BeanException;

    /**
     * Set this property on that bean. Allows excluding of properties
     */
    public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map values);

    /**
     * Transforms the simple basic jcr property value objects to complexer properties
     */
    public Object convertPropertyValue(Class propertyType, Object value) throws Content2BeanException;

    /**
     * The mapping to use
     */
    public TypeMapping getTypeMapping();

    /**
     * Get your instance here
     */
    class Factory {

        public static Content2BeanTransformer getDefaultTransformer() {
            return (Content2BeanTransformer) FactoryUtil.getSingleton(Content2BeanTransformer.class);
        }
    }

}