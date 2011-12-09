/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.decoration;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import info.magnolia.jcr.iterator.FilteringRangeIterator;

/**
 * PropertyIterator that applies wrappers and filtering by delegating to a {@link ContentDecorator}.
 *
 * @version $Id$
 */
public class ContentDecoratorPropertyIterator extends FilteringRangeIterator<Property> implements PropertyIterator {

    private final ContentDecorator contentDecorator;

    public ContentDecoratorPropertyIterator(PropertyIterator propertyIterator, ContentDecorator contentDecorator) {
        super(propertyIterator);
        this.contentDecorator = contentDecorator;
    }

    @Override
    public Property next() {
        return wrapProperty(super.next());
    }

    @Override
    public Property nextProperty() {
        return wrapProperty(super.next());
    }

    @Override
    protected boolean evaluate(Property property) {
        return contentDecorator.evaluateProperty(property);
    }

    protected Property wrapProperty(Property property) {
        return contentDecorator.wrapProperty(property);
    }
}
