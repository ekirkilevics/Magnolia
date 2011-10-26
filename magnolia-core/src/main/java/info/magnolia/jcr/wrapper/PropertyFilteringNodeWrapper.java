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
package info.magnolia.jcr.wrapper;

import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Node wrapper that filters out properties based on a predicate, child nodes acquired via this wrapper are also
 * wrapped in order to filter properties on them as well.
 *
 * @version $Id$
 */
public class PropertyFilteringNodeWrapper extends ChildWrappingNodeWrapper {

    private final AbstractPredicate<Property> predicate;

    public PropertyFilteringNodeWrapper(Node wrapped, AbstractPredicate<Property> predicate) {
        super(wrapped);
        this.predicate = predicate;
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return new FilteringPropertyIterator(super.getProperties(), predicate);
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return new FilteringPropertyIterator(super.getProperties(namePattern), predicate);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return new FilteringPropertyIterator(super.getProperties(nameGlobs), predicate);
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        Property property = super.getProperty(relPath);
        if (!predicate.evaluate(property)) {
            throw new PathNotFoundException("Property " + relPath + " is not accessible via this wrapper.");
        }
        return property;
    }
}
