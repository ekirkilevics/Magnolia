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

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.Calendar;

/**
 * TODO : this is incomplete, please complete per your needs...
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MockMetaData extends MetaData {
    private final MockContent mockContent;

    public MockMetaData(MockContent mockContent) {
        this.mockContent = mockContent;
    }

    @Override
    public String getHandle() {
        return mockContent.getHandle();
    }

    @Override
    public String getLabel() {
        return mockContent.getName();
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return mockContent.getNodeData(name).getBoolean();
    }

    @Override
    public String getStringProperty(String name) {
        return mockContent.getNodeData(name).getString();
    }

    @Override
    public Calendar getDateProperty(String name) {
        return mockContent.getNodeData(name).getDate();
    }

    @Override
    public void setProperty(String name, boolean value) throws AccessDeniedException {
        mockContent.addNodeData(new MockNodeData(name, Boolean.valueOf(value)));
    }

    @Override
    public void setProperty(String name, Calendar value) throws AccessDeniedException {
        mockContent.addNodeData(new MockNodeData(name, value));
    }

    @Override
    public void setProperty(String name, String value) throws AccessDeniedException {
        mockContent.addNodeData(new MockNodeData(name, value));
    }

}
