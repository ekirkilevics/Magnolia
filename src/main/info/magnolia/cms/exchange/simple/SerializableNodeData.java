/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.core.NodeData;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import java.io.Serializable;
import java.io.InputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * Date: Jun 21, 2004
 * Time: 2:32:31 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */


public class SerializableNodeData implements Serializable {


    private String name;
    private int type;

    /* all possible storage */
    Value value;
    String stringValue;
    long longValue;
    double doubleValue;
    boolean booleanValue;
    Calendar dateValue;

    /* binary value will be serialized in a byte array */
    byte[] byteArrayValue;


    private NodeData baseNodeData;



    public SerializableNodeData(NodeData nodeData) throws SerializationException {
        this.baseNodeData = nodeData;
        this.makeSerializable();
        this.baseNodeData = null;
    }



    /**
     * <p>
     * convert NodeData type object to SerializableNodeData
     * </p>
     *
     * */
    private void makeSerializable() throws SerializationException {
        this.setName(this.baseNodeData.getName());
        this.setType(this.baseNodeData.getType());
        this.setData();
    }



    /**
     * <p>
     * Set serializable nodedata value
     *
     * </p>
     * */
    private void setData() throws SerializationException {
        switch(this.getType()) {
            case PropertyType.STRING:
                this.setValue(this.baseNodeData.getString());
                break;
            case PropertyType.LONG:
                this.setValue(this.baseNodeData.getLong());
                break;
            case PropertyType.DOUBLE:
                this.setValue(this.baseNodeData.getDouble());
                break;
            case PropertyType.BOOLEAN:
                this.setValue(this.baseNodeData.getBoolean());
                break;
            case PropertyType.DATE:
                this.setValue(this.baseNodeData.getDate());
                break;
            case PropertyType.BINARY:
                this.setBinaryAsLink(this.baseNodeData.getHandle());
                /*
                try {
                    this.setValue(this.baseNodeData.getValue().getStream());
                } catch (Exception re) { throw new SerializationException(re); }
                */
                break;
            default:
                throw new SerializationException("Unsupported property type");
        }
    }



    public void setName(String name) {
        this.name = name;
    }



    public String getName() {
        return this.name;
    }



    public void setType(int type) {
        this.type = type;
    }



    public int getType() {
        return this.type;
    }



    public void setValue(Value value) {
        this.value = value;
    }


    public Value getValue() {
        return this.value;
    }


    public void setValue(String value) {
        this.stringValue = value;
    }


    public String getString() {
        return this.stringValue;
    }



    public void setValue(long value) {
        this.longValue = value;
    }



    public long getLong() {
        return this.longValue;
    }



    public void setValue(double value) {
        this.doubleValue = value;
    }



    public double getDouble() {
        return this.doubleValue;
    }


    public void setValue(boolean value) {
        this.booleanValue = value;
    }


    public boolean getBoolean() {
        return this.booleanValue;
    }


    public void setValue(Calendar value) {
        this.dateValue = value;
    }


    public Calendar getDate() {
        return this.dateValue;
    }



    public void setValue(InputStream value, int size) throws IOException {
        value.read(this.byteArrayValue,0,size);
    }


    public void setValue(InputStream value) throws IOException {
        int nextByte;
        int index = 0;
        while ((nextByte = value.read()) != -1) {
            this.byteArrayValue[index] = (byte)nextByte;
            index++;
        }
    }


    public void setBinaryAsLink(String value) {
        this.stringValue = value;
    }



    public String getBinaryAsLink() {
        return this.stringValue;
    }


    public byte[] getByteArray() {
        return this.byteArrayValue;
    }


}
