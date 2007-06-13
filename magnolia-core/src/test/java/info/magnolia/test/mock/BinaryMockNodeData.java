/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.beans.runtime.FileProperties;
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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class BinaryMockNodeData extends MockNodeData {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BinaryMockNodeData.class);

    private Map attributes = new HashMap();

    public BinaryMockNodeData(String name, InputStream stream, String fileName, String mimeType, int size) {
        super(name, stream);
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

    public String getAttribute(String name) {
        return (String) attributes.get(name);
    }

    public Collection getAttributeNames() throws RepositoryException {
        return attributes.keySet();
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        setAttribute(name, value.toString());
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        attributes.put(name, value);
    }
}
