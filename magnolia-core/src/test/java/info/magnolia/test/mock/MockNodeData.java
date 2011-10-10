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
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.test.mock.jcr.MockValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;


/**
 * @version $Id$
 */
public class MockNodeData extends DefaultNodeData {

    public MockNodeData(String name, Object value) {
        super(null, name);
        try {
            getJCRProperty().setValue(new MockValue(value));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public MockNodeData(String name, int type) {
        super(null, name);
    }

    @Override
    public InputStream getStream() {
        final String s = getString();
        if (s == null) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return null;
        }
        try {
            return new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHandle() {
        try {
            if(getParent() != null){
                return getParent().getHandle() + "/" + this.getName();
            }
            return this.getName();
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't build handle", e);
        }
    }

    @Override
    public void save() throws RepositoryException {
        // nothing to do
    }

    @Override
    public void delete() throws RepositoryException {
        getParent().deleteNodeData(this.name);
    }

    @Override
    public long getContentLength() {
        return getString().length();
    }

    @Override
    public Property getJCRProperty() {
        if(isExist()){
            return new MockJCRProperty(this);
        }
        throw new RuntimeRepositoryException(new PathNotFoundException(getHandle()));
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        // nothing to do
    }
}
