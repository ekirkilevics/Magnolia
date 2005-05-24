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
package info.magnolia.cms.core.export;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Date: May 24, 2005
 * Time: 10:47:49 AM
 *
 * @author Sameer Charles
 * $Id :$
 */
public interface Export {


    /**
     * if true include only link to the binary properties else embed in the exported data
     * @param binaryAsLink
     * */
    public void setBinaryAsLink(boolean binaryAsLink);

    /**
     * returns true if the binary properties are included as link
     * */
    public boolean getBinaryAsLink();

    /**
     * export content to an object defined by the implementing class
     * @param content to be exported
     * @return Object
     * @throws RepositoryException
     * */
    public Object export(Content content) throws RepositoryException;

    /**
     * export content to the provided output stream
     * @param content to be exported
     * @param outStream
     * @throws RepositoryException
     * @throws IOException
     * */
    public void export(Content content, OutputStream outStream) throws RepositoryException, IOException;

    /**
     * set exporter implementation dependent parameters
     * @param key
     * @param value
     * */
    public void setParameter(String key, Object value);

    /**
     * get parameter previously set on this exporter
     * @return Object
     * */
    public Object getParameter(String key);
}
