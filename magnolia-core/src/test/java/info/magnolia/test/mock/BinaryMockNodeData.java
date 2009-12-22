/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Content;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.ArrayList;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class BinaryMockNodeData extends MockNodeData {
    private final Map<String, String> attributes;
    private final MockContent wrappedContent;

    public BinaryMockNodeData(String name, InputStream stream, String fileName, String mimeType, int size) throws RepositoryException {
        super(name, stream);
        attributes = new HashMap<String, String>();
        setAttribute(FileProperties.PROPERTY_FILENAME, StringUtils.substringBeforeLast(fileName, "."));
        setAttribute(FileProperties.PROPERTY_EXTENSION, StringUtils.substringAfterLast(fileName, "."));
        setAttribute(FileProperties.PROPERTY_SIZE, "" + size);
        setAttribute(FileProperties.PROPERTY_CONTENTTYPE, mimeType);

        final Calendar lastMod = new GregorianCalendar(TimeZone.getDefault());
        setAttribute(FileProperties.PROPERTY_LASTMODIFIED, lastMod);
        wrappedContent = null;
    }

    public BinaryMockNodeData(Content wrappedContent) {
        this((MockContent) wrappedContent);
    }

    public BinaryMockNodeData(MockContent wrappedContent) {
        super(wrappedContent.getName(), null);
        setParent(wrappedContent.getParent());
        // we'll delegate to wrappedContent
        this.attributes = null;
        this.wrappedContent = wrappedContent;
    }

    public int getType() {
        return PropertyType.BINARY;
    }

    public String getAttribute(String name) {
        return attributes != null ? attributes.get(name) : wrappedContent.getNodeData(name).getString();
    }

    public Collection<String> getAttributeNames() throws RepositoryException {
        if (attributes != null) {
            return attributes.keySet();
        } else {
            final Collection<NodeData> datas = wrappedContent.getNodeDataCollection();
            final Collection<String> names = new ArrayList<String>();
            for (NodeData data : datas) {
                final String name = data.getName();
                if (!name.equalsIgnoreCase(ItemType.JCR_DATA)) {
                    names.add(name);
                }
            }
            return names;
        }
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        setAttribute(name, value.toString());
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        attributes.put(name, value);
    }
}
