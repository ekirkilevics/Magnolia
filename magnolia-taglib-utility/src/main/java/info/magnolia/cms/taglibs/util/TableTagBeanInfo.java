/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;


/**
 * Bean info, needed for the "class" attribute.
 * @author Fabrizio Giustina
 * @version $Revision: 2985 $ ($Author: fgiust $)
 */
public class TableTagBeanInfo extends SimpleBeanInfo
{

    /**
     * List of exposed properties.
     */
    private String[] properties = new String[]{"header", "class", "style", "id", "cellspacing", "cellpadding"};

    /**
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors()
    {

        try
        {
            List proplist = new ArrayList();
            for (int j = 0; j < properties.length; j++)
            {
                proplist.add(createPropertyDescriptor(properties[j]));
            }
            PropertyDescriptor[] result = new PropertyDescriptor[proplist.size()];
            return ((PropertyDescriptor[]) proplist.toArray(result));

        }
        catch (IntrospectionException ex)
        {
            // should never happen
            throw new UnhandledException(ex.getMessage(), ex);
        }

    }

    /**
     * Instantiate a property descriptor for the given property.
     * @param propertyName property name
     * @return property descriptor
     * @throws IntrospectionException if the given property is not valid
     */
    private PropertyDescriptor createPropertyDescriptor(String propertyName) throws IntrospectionException
    {
        return new PropertyDescriptor(propertyName, TableTag.class, null, "set" + StringUtils.capitalize(propertyName));
    }

}
