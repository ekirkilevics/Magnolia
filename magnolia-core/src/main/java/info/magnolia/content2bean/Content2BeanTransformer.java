package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;

/**
 * @author philipp
 * @version $Id$
 *
 */
public interface Content2BeanTransformer {

    public Class getClassForCollectionProperty(Class parentClass, String name);

    public void addCollectionPropertyClass(Class type, String name, Class mappedType);

    public Class resolveClass(Content node) throws ClassNotFoundException;

    public void pushBean(Object bean);

    public void popBean();

    public void setProperty(Object bean, String propertyName, Object object);

}