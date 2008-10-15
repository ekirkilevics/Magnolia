/**
 * This file Copyright (c) 2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;


/**
 * @author pbracher
 * @version $Id$
 */
public class InheritanceContentWrapper extends ContentWrapper {

    private static Logger log = LoggerFactory.getLogger(InheritanceContentWrapper.class);

    public InheritanceContentWrapper(Content node) {
        super(node);
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return getNodeData(name).isExist();
    }

    public NodeData getNodeData(String name) {
        try {
            if (getWrappedContent().hasNodeData(name)) {
                return getWrappedContent().getNodeData(name);
            }
            else {
                if(getLevel()>0){
                    // continious inheritance
                    return getParent().getNodeData(name);
                }
            }
        }
        catch (RepositoryException e) {
            log.error("Can't inherit nodedata", e);
        }
        // creates a none existing node data in the standard manner
        return getWrappedContent().getNodeData(name);
    }

    protected Content wrap(Content node) {
        return new InheritanceContentWrapper(node);
    }
}