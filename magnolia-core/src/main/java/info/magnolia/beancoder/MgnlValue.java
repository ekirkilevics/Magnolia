/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.beancoder;

import java.io.InputStream;

import javax.jcr.RepositoryException;

import openwfe.org.jcr.JcrException;
import openwfe.org.jcr.JcrProxy;
import openwfe.org.jcr.Value;


/**
 * Magnolia Wrapper for value
 */
public class MgnlValue implements Value {

    javax.jcr.Value v;

    public Object getWrappedInstance() throws JcrException {
        return v;
    }

    public MgnlValue(javax.jcr.Value v) {
        this.v = v;
    }

    public String getString() throws JcrException {
        try {
            return v.getString();
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public int getType() {
        return v.getType();
    }

    public long getLong() throws JcrException {
        try {
            return v.getLong();
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage(), e);
        }
    }

    public Object getValue() throws JcrException {
        return JcrProxy.wrapJcrValue(v).getValue();
    }

    /**
     * @see openwfe.org.jcr.Value#getStream()
     */
    public InputStream getStream() throws JcrException {
        try {
            return v.getStream();
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage(), e);
        }
    }
}
