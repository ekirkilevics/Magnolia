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
package info.magnolia.jcr.iterator;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.wrapper.PropertyWrapperFactory;

/**
 * PropertyIterator hiding all properties that do not pass the predicate, returned properties can also be wrapped.
 *
 * @version $Id$
 */
public class FilteringPropertyIterator extends FilteringRangeIterator<Property> implements PropertyIterator {

    private PropertyWrapperFactory wrapperFactory;
    private AbstractPredicate<Property> predicate;

    public FilteringPropertyIterator(PropertyIterator propertyIterator, AbstractPredicate<Property> predicate) {
        super(propertyIterator);
        this.predicate = predicate;
    }

    public FilteringPropertyIterator(PropertyIterator propertyIterator, AbstractPredicate predicate, PropertyWrapperFactory wrapperFactory) {
        super(propertyIterator);
        this.wrapperFactory = wrapperFactory;
        this.predicate = predicate;
    }

    @Override
    public Property next() {
        return wrapProperty(super.next());
    }

    @Override
    public Property nextProperty() {
        return wrapProperty(super.next());
    }

    protected Property wrapProperty(Property property) {
        return wrapperFactory != null ? wrapperFactory.wrapProperty(property) : property;
    }

    @Override
    protected boolean evaluate(Property property) {
        return predicate == null || predicate.evaluate(property);
    }
}
