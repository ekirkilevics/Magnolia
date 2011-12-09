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
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import info.magnolia.jcr.wrapper.DelegatePropertyWrapper;

/**
 * Property wrapper that applies wrappers and filtering by delegating to a {@link ContentDecorator}.
 *
 * @version $Id$
 */
public class ContentDecoratorPropertyWrapper extends DelegatePropertyWrapper {

    private final ContentDecorator contentDecorator;

    public ContentDecoratorPropertyWrapper(Property property, ContentDecorator contentDecorator) {
        super(property);
        this.contentDecorator = contentDecorator;
    }

    @Override
    public Session getSession() throws RepositoryException {
        return wrapSession(super.getSession());
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
}
