/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.test.mock;

import info.magnolia.cms.core.DefaultNodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.exception.RuntimeRepositoryException;

import javax.jcr.RepositoryException;


/**
 * @version $Id$
 */
public class MockNodeData extends DefaultNodeData {


    public MockNodeData(MockContent content, String name, Object value) {
        super(content, name);
        try {
            NodeDataUtil.setValue(this, value);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * @deprecated since 4.5 - don't use it at all will: add test-MockContent so e.g. the property value will be added to this one!!!
     */
    public MockNodeData(String name, Object value) {
        this(new MockContent("test"), name, value);
    }

    public MockNodeData(String name, int type) {
        super(new MockContent("test"), name);
    }

    // TODO dlipp - type is ignored here - check it out!
    public MockNodeData(MockContent parent, String name, int type) {
        super(parent, name);
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null || !(arg0 instanceof MockNodeData)) {
            return false;
        }
        MockNodeData other = (MockNodeData) arg0;
        if (!getName().equals(other.getName())) {
            return false;
        }
        if (!getValue().equals(other.getValue())) {
            return false;
        }
        try {
            if (!getParent().equals(other.getParent())) {
                return false;
            }
        } catch (RepositoryException e) {
            return false;
        }
        return true;
    }
}
