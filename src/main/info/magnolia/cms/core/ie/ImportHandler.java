/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core.ie;

import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;


/**
 * Date: May 24, 2005 Time: 4:48:26 PM
 * @author Sameer Charles $Id :$
 */
public interface ImportHandler {

    /**
     * If true, this importer assumes that binary data is not embedded in main data file
     * @param binaryAsLink
     */
    public void setBinaryAsLink(boolean binaryAsLink);

    /**
     * returns true if the binary properties are included as link
     */
    public boolean getBinaryAsLink();

    /**
     * import content from the given input stream
     * @param target where this content stream should be imported
     * @param inStream
     * @throws RepositoryException
     * @throws java.io.IOException
     */
    public void importContent(Content target, InputStream inStream) throws RepositoryException, IOException;

    /**
     * set importer implementation dependent parameters
     * @param key
     * @param value
     */
    public void setParameter(String key, Object value);

    /**
     * get parameter previously set on this importer
     * @return Object
     */
    public Object getParameter(String key);
}
