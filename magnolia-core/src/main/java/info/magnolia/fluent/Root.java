/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.fluent;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Root {
    private final Content root;
    private final NodeOperation[] childrenOps;

    public Root(Content root, NodeOperation... childrenOps) {
        this.root = root;
        this.childrenOps = childrenOps;
    }

    public void exec() throws RepositoryException {
        for (NodeOperation childrenOp : childrenOps) {
            childrenOp.exec(root);
        }
    }

    // TODO some context passed around, configuration at beginning
    // (what to do with exceptions, what to do with warnings

}
