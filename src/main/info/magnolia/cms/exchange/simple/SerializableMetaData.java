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

import info.magnolia.cms.core.MetaData;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Date: Aug 5, 2004
 * Time: 4:57:42 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */


public class SerializableMetaData implements Serializable {



    private static Logger log = Logger.getLogger(SerializableMetaData.class);

    private MetaData baseMetaData;

    /* meta properties */
    private ArrayList metaProperties = new ArrayList();


    public SerializableMetaData(MetaData metaData) {
        this.baseMetaData = metaData;
        this.makeSerializable();
        this.baseMetaData = null;
    }



    public ArrayList getMetaProperties() {
        return this.metaProperties;
    }



    private void makeSerializable() {
        PropertyIterator pi = this.baseMetaData.getProperties();
        if (pi == null)
            return;
        while(pi.hasNext()) {
            try {
                Property property = (Property) pi.next();
                if (!property.hasValue())
                    continue;
                int type = property.getValue().getType();
                MetaDataProperty metaProperty = new MetaDataProperty(property.getName(),type);
                switch (type) {
                    case PropertyType.STRING:
                        metaProperty.setValue(property.getString());
                        break;
                    case PropertyType.LONG:
                        metaProperty.setValue(property.getLong());
                        break;
                    case PropertyType.DOUBLE:
                        metaProperty.setValue(property.getDouble());
                        break;
                    case PropertyType.BOOLEAN:
                        metaProperty.setValue(property.getBoolean());
                        break;
                    case PropertyType.DATE:
                        metaProperty.setValue(property.getDate());
                    default:
                        // name is not sent as property type, but as an ID of this object
                }
                if (!(metaProperty.getValue() == null))
                    this.metaProperties.add(metaProperty);
                property = null;
            } catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
    }


}


class MetaDataProperty implements Serializable {


    private String name;
    private Object value;

    /* JCR property type */
    private int type;



    private String stringValue;
    private long longValue;
    private double doubleValue;
    private boolean booleanValue;
    private Calendar dateValue;


    MetaDataProperty(String name, int type) {
        this.name = name;
        this.type = type;
    }



    MetaDataProperty(String name, int type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }


    public String getName() {
        return this.name;
    }


    public int getType() {
        return this.type;
    }


    Object getValue() {
        return this.value;
    }


    public void setValue(String value) {
        this.stringValue = value;
    }


    public void setValue(long value) {
        this.longValue = value;
    }


    public void setValue(double value) {
        this.doubleValue = value;
    }


    public void setValue(boolean value) {
        this.booleanValue = value;
    }


    public void setValue(Calendar value) {
        this.dateValue = value;
    }


    public String getString() {
        return this.stringValue;
    }


    public long getLong() {
        return this.longValue;
    }


    public double getDouble() {
        return this.doubleValue;
    }


    public boolean getBoolean() {
        return this.booleanValue;
    }


    public Calendar getDate() {
        return this.dateValue;
    }



}
