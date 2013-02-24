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

import info.magnolia.jcr.wrapper.DelegateNodeWrapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

/**
 * Node wrapper that applies wrappers and filtering by delegating to a {@link ContentDecorator}.
 *
 * @version $Id$
 */
public class ContentDecoratorNodeWrapper extends DelegateNodeWrapper {

    private final ContentDecorator contentDecorator;

    public ContentDecoratorNodeWrapper(Node node, ContentDecorator contentDecorator) {
        super(node);
        this.contentDecorator = contentDecorator;
    }

    public ContentDecorator getContentDecorator() {
        return contentDecorator;
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
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return wrapNode(super.addNode(relPath));
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return wrapNode(super.addNode(relPath, primaryNodeTypeName));
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        Node node = super.getNode(relPath);
        if (!contentDecorator.evaluateNode(node)) {
            throw new PathNotFoundException(relPath);
        }
        return wrapNode(node);
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return wrapNodeIterator(super.getNodes());
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return wrapNodeIterator(super.getNodes(namePattern));
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return wrapNodeIterator(super.getNodes(nameGlobs));
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return super.hasNode(relPath) && contentDecorator.evaluateNode(super.getNode(relPath));
    }

    @Override
    public boolean hasNodes() throws RepositoryException {
        return getNodes().hasNext();
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return wrapPropertyIterator(super.getProperties());
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return wrapPropertyIterator(super.getProperties(namePattern));
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return wrapPropertyIterator(super.getProperties(nameGlobs));
    }

    @Override
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return wrapPropertyIterator(super.getWeakReferences());
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return wrapProperty(super.getProperty(relPath));
    }

    @Override
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return wrapPropertyIterator(super.getReferences(name));
    }

    @Override
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return wrapPropertyIterator(super.getWeakReferences(name));
    }

    @Override
    public PropertyIterator getReferences() throws RepositoryException {
        return wrapPropertyIterator(super.getReferences());
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value, type));
    }

    @Override
    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, values));
    }

    @Override
    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, values, type));
    }

    @Override
    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value));
    }

    @Override
    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, value, type));
    }

    @Override
    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, values));
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return wrapProperty(super.setProperty(name, values, type));
    }

    protected Session wrapSession(Session session) {
        return contentDecorator.wrapSession(session);
    }

    protected Node wrapNode(Node node) {
        return contentDecorator.wrapNode(node);
    }

    protected NodeIterator wrapNodeIterator(NodeIterator nodeIterator) {
        return contentDecorator.wrapNodeIterator(nodeIterator);
    }

    protected Property wrapProperty(Property property) {
        return contentDecorator.wrapProperty(property);
    }

    protected PropertyIterator wrapPropertyIterator(PropertyIterator propertyIterator) {
        return contentDecorator.wrapPropertyIterator(propertyIterator);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ContentDecoratorNodeWrapper)) {
            return false;
        }
        ContentDecoratorNodeWrapper that = (ContentDecoratorNodeWrapper) obj;
        return (this.wrapped == null ? that.wrapped == null : this.wrapped.equals(that.wrapped))
                && this.contentDecorator == null ? that.contentDecorator == null : this.contentDecorator.equals(that.contentDecorator);
    }

    @Override
    public int hashCode() {
        return (this.wrapped == null ? 7 : this.wrapped.hashCode()) + (this.contentDecorator == null ? 6 : this.contentDecorator.hashCode());
    }
}
