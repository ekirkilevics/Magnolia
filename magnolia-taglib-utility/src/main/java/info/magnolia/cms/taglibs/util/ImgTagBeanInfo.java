/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
 * @version $Revision$ ($Author$)
 */
public class ImgTagBeanInfo extends SimpleBeanInfo {

    /**
     * List of exposed properties.
     */
    private String[] properties = new String[]{
        "nodeDataName",
        "contentNode",
        "contentNodeName",
        "contentNodeCollectionName",
        "inherit",
        "altNodeDataName",
        "class",
        "style",
        "id",
        "width",
        "height"};

    /**
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            List proplist = new ArrayList();
            for (int j = 0; j < properties.length; j++) {
                proplist.add(createPropertyDescriptor(properties[j]));
            }
            PropertyDescriptor[] result = new PropertyDescriptor[proplist.size()];
            return ((PropertyDescriptor[]) proplist.toArray(result));

        }
        catch (IntrospectionException ex) {
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
    private PropertyDescriptor createPropertyDescriptor(String propertyName) throws IntrospectionException {
        return new PropertyDescriptor(propertyName, ImgTag.class, null, "set" + StringUtils.capitalize(propertyName));
    }

}
