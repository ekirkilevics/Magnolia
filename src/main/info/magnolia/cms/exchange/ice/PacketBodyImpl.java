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
package info.magnolia.cms.exchange.ice;

import info.magnolia.exchange.PacketBody;
import info.magnolia.exchange.PacketIOException;
import info.magnolia.exchange.PacketType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;


/**
 * @author Sameer Charles
 */
public class PacketBodyImpl implements PacketBody {

    private int type;

    private StringBuffer data;

    public PacketBodyImpl() {
        this.data = new StringBuffer();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setLength(long size) {
    }

    public long getLength() {
        return this.data.length();
    }

    public void setBody(String data) {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.STRING);
        this.data.append(data);
    }

    public void setBody(InputStream data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.BINARY);
        byte[] buffer = new byte[8192];
        try {
            while ((data.read(buffer)) > 0) {
                this.data.append(buffer);
            }
            data.close();
        }
        catch (IOException e) {
            throw (new PacketIOException(e));
        }
    }

    public void setBody(Long data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.LONG);
        this.data.append(data.toString());
    }

    public void setBody(Double data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.DOUBLE);
        this.data.append(data.toString());
    }

    public void setBody(Calendar data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.DATE);
        this.data.append(data.getTime().toString());
    }

    public void setBody(Boolean data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.BOOLEAN);
        this.data.append(data.toString());
    }

    public void setBody(Object data) throws PacketIOException {
        if (data == null) {
            throw (new IllegalArgumentException());
        }
        this.setType(PacketType.OBJECT);
        // todo ?? handle object types
        // this.data.append(data.toString());
    }

    public String toString() {
        return this.data.toString();
    }

    public Object getObject() {
        // todo
        return null;
    }
}
