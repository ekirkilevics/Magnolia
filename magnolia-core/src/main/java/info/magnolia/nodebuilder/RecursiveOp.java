/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.nodebuilder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeTypeFilter;
import info.magnolia.nodebuilder.Ops.AbstractOp;

import javax.jcr.RepositoryException;

/**
 * Visits the hierarchy recursively and executes the operations on all nodes matching the filter. The recursion does not stop if a node does not match!
 * The class has some static methods to build recursive operations easily.
 * @author pbracher
 * @version $Id$
 *
 */
public class RecursiveOp extends AbstractOp {
    
    /**
     * Visits the hierarchy recursively and executes the operations on all nodes excluding meta data and jcr base nodes. The recursion does not stop if a node does not match!
     */
    public static NodeOperation recursive(final NodeOperation... childrenOps){
        return recursive(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER, childrenOps);
    }

    /**
     * Visits the hierarchy recursively and executes the operations on all nodes matching a certain type. The recursion does not stop if a node does not match!
     */
    public static NodeOperation recursive(final String type, final NodeOperation... childrenOps){
        return recursive(new NodeTypeFilter(type), childrenOps);
    }

    /**
     * Visits the hierarchy recursively and executes the operations on all nodes matching a certain type. The recursion does not stop if a node does not match!
     */
    public static NodeOperation recursive(final ItemType type, final NodeOperation... childrenOps){
        return recursive(new NodeTypeFilter(type), childrenOps);
    }

    public static NodeOperation recursive(final Content.ContentFilter filter, final NodeOperation... childrenOps){
        return new RecursiveOp(filter, childrenOps);
    }

    private final ContentFilter filter;

    private final NodeOperation[] childrenOps;

    /**
     * Visits the hierarchy recursively and executes the operations on all nodes matching the filter. The recursion does not stop if a node does not match!
     */
    public RecursiveOp(ContentFilter filter, NodeOperation[] childrenOps) {
        this.filter = filter;
        this.childrenOps = childrenOps;
    }

    Content doExec(Content context,final ErrorHandler errorHandler) throws RepositoryException {
        try {
            ContentUtil.visit(context, new ContentUtil.Visitor(){
                public void visit(Content node) throws Exception {
                    if(filter.accept(node)){
                        for (NodeOperation nodeOperation : childrenOps) {
                            nodeOperation.exec(node,errorHandler);
                        }
                    }
                }
            }, ContentUtil.ALL_NODES_CONTENT_FILTER);
        }
        catch (Exception e) {
            if(e instanceof RepositoryException){
                throw (RepositoryException) e;
            }
            else{
                throw new RuntimeException(e);
            }
        }
        return context;
    }
}