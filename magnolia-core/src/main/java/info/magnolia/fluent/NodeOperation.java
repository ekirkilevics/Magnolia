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
public interface NodeOperation {
    // return this
    // TODO - differentiate between operations that can be chained or not - i.e setProperty shouldnt be chainable
    NodeOperation then(NodeOperation... childrenOps);

    // TODO exec should not appear in public interface
    void exec(Content context) throws RepositoryException;
}
