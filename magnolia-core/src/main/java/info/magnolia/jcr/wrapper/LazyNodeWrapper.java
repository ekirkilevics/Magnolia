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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.cms.util.DelegateNodeWrapper;
import info.magnolia.context.MgnlContext;


/**
 * Node wrapper that will reacquire the node if its session is closed.
 *
 * @version $Id$
 */
public class LazyNodeWrapper extends DelegateNodeWrapper {

    private final String workspace;
    private final String nodeIdentifier;
    private transient Node node;

    public LazyNodeWrapper(String workspace, String nodeIdentifier) {
        this.workspace = workspace;
        this.nodeIdentifier = nodeIdentifier;
    }

    public LazyNodeWrapper(Node node) throws RepositoryException {
        this.workspace = node.getSession().getWorkspace().getName();
        this.nodeIdentifier = node.getIdentifier();
        this.node = node;
    }

    @Override
    public synchronized Node getWrappedNode() throws RepositoryException {
        if (node == null || !node.getSession().isLive()) {
            Session session = getSessionForWrappedNode(this.workspace);
            node = session.getNodeByIdentifier(this.nodeIdentifier);
        }
        return node;
    }

    protected Session getSessionForWrappedNode(String workspace) throws RepositoryException {
        return MgnlContext.getSystemContext().getJCRSession(workspace, workspace);
    }
}
