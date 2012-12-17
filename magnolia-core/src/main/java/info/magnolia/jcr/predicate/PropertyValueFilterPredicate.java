/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.jcr.predicate;


import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

/**
 * Simple predicate implementation that filter property based on the desired value.
 * Return only properties that have a value equals to the value passed as constructor.
 * If value is set to null, return only properties that have a null value.
 * If the property is not of the type {@PropertyType.STRING} return false.
 */
public class PropertyValueFilterPredicate extends AbstractPredicate<Property> {

    private String value;

    public PropertyValueFilterPredicate(String value) {
        this.value = value;
    }

    @Override
    public boolean evaluateTyped(Property property) {
        try {
            if(value == null) {
                return property.getString() == null;
            } else if(property.getValue() == null || property.getType() != PropertyType.STRING) {
                return false;
            } else {
                return value.equals(property.getString());
            }
        } catch (RepositoryException e) {
            // either invalid or not accessible to the current user
            return false;
        }
    }
}
