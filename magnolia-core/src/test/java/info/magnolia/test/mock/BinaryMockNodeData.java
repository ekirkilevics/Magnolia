/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class BinaryMockNodeData extends MockNodeData {
    private static final Logger log = LoggerFactory.getLogger(BinaryMockNodeData.class);

    // BinaryMockNodeData are instantiated either with an InputStream and specific properties
    private Map<String, String> attributes = new HashMap<String, String>();
    // or by wrapping a Content instance (used i.e when creating mock content via properties)
    private MockContent wrappedContent;

    public BinaryMockNodeData(String name) {
        super(name, PropertyType.BINARY);
    }

    public BinaryMockNodeData(String name, MockContent wrappedContent) {
        this(name);
        this.wrappedContent = wrappedContent;
    }

    public BinaryMockNodeData(String name, InputStream stream) {
        super(name, stream);
    }

    public BinaryMockNodeData(String name, InputStream stream, String fileName, String mimeType, int size) {
        this(name, stream);
        try {
            setAttribute(FileProperties.PROPERTY_FILENAME, StringUtils.substringBeforeLast(fileName, "."));
            setAttribute(FileProperties.PROPERTY_EXTENSION, StringUtils.substringAfterLast(fileName, "."));
            setAttribute(FileProperties.PROPERTY_SIZE, "" + size);
            setAttribute(FileProperties.PROPERTY_CONTENTTYPE, mimeType);

            Calendar value = new GregorianCalendar(TimeZone.getDefault());
            setAttribute(FileProperties.PROPERTY_LASTMODIFIED, value);
        }
        catch (RepositoryException e) {
            // should really not happen
            log.error("can't initialize binary mock node data", e);
        }
    }

    public int getType() {
        return PropertyType.BINARY;
    }

    public InputStream getStream() {
        if (wrappedContent != null) {
            return wrappedContent.getNodeData(ItemType.JCR_DATA).getStream();
        }

        return super.getStream();
    }

    public String getString() {
        if (wrappedContent != null) {
            return wrappedContent.getNodeData(ItemType.JCR_DATA).getString();
        }
        return super.getString();
    }

    public String getAttribute(String name) {
        if (wrappedContent != null) {
            return wrappedContent.getNodeData(name).getString();
        }
        return attributes.get(name);
    }

    public Collection<String> getAttributeNames() throws RepositoryException {
        if (wrappedContent != null) {
            return CollectionUtils.transformedCollection(wrappedContent.getNodeDataCollection(), new Transformer() {
                public Object transform(Object input) {
                    return ((MockNodeData) input).getName();
                }
            });
        }
        return attributes.keySet();
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        if (wrappedContent != null) {
            throw new UnsupportedOperationException();
        }
        setAttribute(name, value.toString());
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        if (wrappedContent != null) {
            throw new UnsupportedOperationException();
        }
        attributes.put(name, value);
    }

    // unsupported operations, copied from BinaryNockData

    public Calendar getDate() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public boolean getBoolean() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public double getDouble() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public long getLong() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public Value[] getValues() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(String value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(int value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(long value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(double value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(boolean value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Calendar value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Content value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Value value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Value[] value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }
}
