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
package info.magnolia.jcr.decoration;

import info.magnolia.jcr.wrapper.DelegatePropertyWrapper;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

/**
 * Property wrapper that applies wrappers and filtering by delegating to a {@link ContentDecorator}.
 * 
 * @param <D> decorator implementation.
 */
public class ContentDecoratorPropertyWrapper<D extends ContentDecorator> extends DelegatePropertyWrapper {

    private final D contentDecorator;

    public ContentDecoratorPropertyWrapper(Property property, D contentDecorator) {
        super(property);
        this.contentDecorator = contentDecorator;
    }

    @Override
    public Session getSession() throws RepositoryException {
        return wrapSession(super.getSession());
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        Item item = super.getAncestor(depth);
        if (item.isNode()) {
            return wrapNode((Node) item);
        } else {
            return wrapProperty((Property) item);
        }
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return wrapNode(super.getParent());
    }

    @Override
    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        Node node = super.getNode();
        if (!contentDecorator.evaluateNode(node)) {
            throw new ItemNotFoundException();
        }
        return wrapNode(node);
    }

    @Override
    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        Property property = super.getProperty();
        if (!contentDecorator.evaluateProperty(property)) {
            throw new ItemNotFoundException();
        }
        return wrapProperty(property);
    }

    protected Session wrapSession(Session session) {
        return contentDecorator.wrapSession(session);
    }

    protected Node wrapNode(Node node) {
        return contentDecorator.wrapNode(node);
    }

    protected Property wrapProperty(Property property) {
        return contentDecorator.wrapProperty(property);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ContentDecoratorPropertyWrapper)) {
            return false;
        }
        ContentDecoratorPropertyWrapper that = (ContentDecoratorPropertyWrapper) obj;
        return (this.getWrappedProperty() == null ? that.getWrappedProperty() == null : this.getWrappedProperty().equals(that.getWrappedProperty()))
                && this.contentDecorator == null ? that.contentDecorator == null : this.contentDecorator.equals(that.contentDecorator);
    }

    @Override
    public int hashCode() {
        return (this.getWrappedProperty() == null ? 7 : this.getWrappedProperty().hashCode()) + (this.contentDecorator == null ? 6 : this.contentDecorator.hashCode());
    }

    public D getContentDecorator() {
        return contentDecorator;
    }
}
