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

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import info.magnolia.jcr.wrapper.DelegateSessionWrapper;

/**
 * Session wrapper that applies wrappers and filtering by delegating to a {@link ContentDecorator}.
 *
 * @version $Id$
 */
public class ContentDecoratorSessionWrapper extends DelegateSessionWrapper {

    private final ContentDecorator contentDecorator;

    public ContentDecoratorSessionWrapper(Session session, ContentDecorator contentDecorator) {
        super(session);
        this.contentDecorator = contentDecorator;
    }

    @Override
    public boolean itemExists(String absPath) throws RepositoryException {
        boolean exists = super.itemExists(absPath);
        if (!exists) {
            return false;
        }
        Item item = super.getItem(absPath);
        if (item.isNode()) {
            return contentDecorator.evaluateNode((Node) item);
        } else {
            return contentDecorator.evaluateProperty((Property) item);
        }
    }

    @Override
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        Item item = super.getItem(absPath);
        if (item.isNode()) {
            if (!contentDecorator.evaluateNode((Node) item)) {
                throw new PathNotFoundException(absPath);
            }
            return wrapNode((Node) item);
        } else {
            if (!contentDecorator.evaluateProperty((Property) item)) {
                throw new PathNotFoundException(absPath);
            }
            return wrapProperty((Property) item);
        }
    }

    @Override
    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        if (!itemExists(absPath)) {
            throw new PathNotFoundException(absPath);
        }
        super.removeItem(absPath);
    }

    @Override
    public Node getRootNode() throws RepositoryException {
        return wrapNode(super.getRootNode());
    }

    @Override
    public boolean nodeExists(String absPath) throws RepositoryException {
        return super.nodeExists(absPath) && contentDecorator.evaluateNode(super.getNode(absPath));
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        Node node = super.getNode(absPath);
        if (!contentDecorator.evaluateNode(node)) {
            throw new PathNotFoundException(absPath);
        }
        return wrapNode(node);
    }

    @Override
    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        Node node = super.getNodeByIdentifier(id);
        if (!contentDecorator.evaluateNode(node)) {
            throw new ItemNotFoundException(id);
        }
        return wrapNode(node);
    }

    @Override
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        Node node = super.getNodeByUUID(uuid);
        if (!contentDecorator.evaluateNode(node)) {
            throw new ItemNotFoundException(uuid);
        }
        return wrapNode(node);
    }

    @Override
    public boolean propertyExists(String absPath) throws RepositoryException {
        return super.propertyExists(absPath) && contentDecorator.evaluateProperty(super.getProperty(absPath));
    }

    @Override
    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        Property property = super.getProperty(absPath);
        if (!contentDecorator.evaluateProperty(property)) {
            throw new PathNotFoundException(absPath);
        }
        return wrapProperty(property);
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        if (!nodeExists(srcAbsPath)) {
            throw new PathNotFoundException(srcAbsPath);
        }
        super.move(srcAbsPath, destAbsPath);
    }

    protected Node wrapNode(Node node) {
        return contentDecorator.wrapNode(node);
    }

    protected Property wrapProperty(Property property) {
        return contentDecorator.wrapProperty(property);
    }
}
