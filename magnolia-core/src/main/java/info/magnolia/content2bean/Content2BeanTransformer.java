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

import java.util.Map;

import info.magnolia.cms.core.Content;


/**
 * Used to create the bens and resolve classes
 * @author philipp
 * @version $Id$
 */
public interface Content2BeanTransformer extends Content.ContentFilter {

    /**
     * Resolves the class to use for that node
     */
    public Class resolveClass(Content node) throws ClassNotFoundException;

    /**
     * Instantiates the bean
     */
    public Object newBeanInstance(Class klass, Map properties);

    /**
     * Called after all properties are set
     */
    public void initBean(Object bean, Map properties) throws Content2BeanException;

    /**
     * Set this property on that bean. Allows conversions or excluding properties
     */
    public void setProperty(Object bean, String propertyName, Object object);

    /**
     * In case the property is a map this method is used to resolve the class to use
     */
    public Class getClassForCollectionProperty(Class parentClass, String name);

    /**
     * Define a collection/map name to class mapping
     */
    public void addCollectionPropertyClass(Class type, String name, Class mappedType);

    /**
     * Push the current bean. This allows to use the bean stack in other methods
     */
    public void pushClass(Class klass);

    /**
     * Pop the current bean
     */
    public void popClass();

    /**
     * Push the current node. This allows to use the content stack in other methods
     */
    public void pushContent(Content node);

    /**
     * Pop the current node
     */
    public void popContent();

}