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
package info.magnolia.cms.util;

import java.lang.reflect.InvocationTargetException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.Predicate;

/**
 * Node wrapper passing on Predicate to its children to hide properties.
 * @author had
 * @version $Id: $
 */
public class PropertiesFilteringNodeWrapper extends ChildWrappingNodeWrapper {

    private final Predicate predicate;

    public PropertiesFilteringNodeWrapper(Node wrapped, Predicate predicate) {
        super(wrapped, PropertiesFilteringNodeWrapper.class);
        this.predicate = predicate;
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return new FilteringPropertyIterator(super.getProperties(), predicate);
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return  new FilteringPropertyIterator(super.getProperties(namePattern), predicate);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return  new FilteringPropertyIterator(super.getProperties(nameGlobs), predicate);
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        Property prop = super.getProperty(relPath);
        if (predicate.evaluate(prop)) {
            return prop;
        }
        throw new PathNotFoundException("Property " + relPath + " is not accesible via this wrapper.");
    }

    @Override
    public Node wrap(Node node) {
        try {
            return this.getClass().getConstructor(Node.class, Predicate.class).newInstance(node, predicate);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
