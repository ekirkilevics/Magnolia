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
package info.magnolia.cms.exchange.simple;

import info.magnolia.exchange.PacketBody;
import info.magnolia.exchange.PacketIOException;
import info.magnolia.exchange.PacketType;

import java.io.InputStream;
import java.util.Calendar;

import org.apache.log4j.Logger;


/**
 * Date: May 4, 2004 Time: 5:10:21 PM
 * @author Sameer Charles
 */
public class PacketBodyImpl implements PacketBody {

    private static Logger log = Logger.getLogger(PacketBodyImpl.class);

    private int type;

    private Object data;

    public PacketBodyImpl() {
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setLength(long size) {
        log.debug("Method not implemented ( setlength() )");
    }

    public long getLength() {
        log.debug("Method not implemented ( getlength() )");
        return 0;
    }

    public void setBody(String data) {
        log.debug("Method not implemented ( setBody(String) )");
    }

    public void setBody(InputStream data) throws PacketIOException {
        log.debug("Method not implemented ( setBody(InputStream) )");
    }

    public void setBody(Long data) throws PacketIOException {
        log.debug("Method not implemented ( setBody(Long) )");
    }

    public void setBody(Double data) throws PacketIOException {
        log.debug("Method not implemented ( setBody(Double) )");
    }

    public void setBody(Calendar data) throws PacketIOException {
        log.debug("Method not implemented ( setBody(Calendar) )");
    }

    public void setBody(Boolean data) throws PacketIOException {
        log.debug("Method not implemented ( setBody(Boolean) )");
    }

    public void setBody(Object data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.OBJECT);
        this.data = data;
    }

    public Object getObject() {
        return this.data;
    }

    public String toString() {
        return this.data.toString();
    }
}
