/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.context.MgnlContext;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property-related utility methods.
 *
 * @version $Id$
 */
public class PropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(PropertyUtil.class);

    public static void renameProperty(Property property, String newName) throws RepositoryException {
        Node node = property.getParent();
        node.setProperty(newName, property.getValue());
        property.remove();
    }

    /**
     * Allows setting a Node's property from an object.
     */
    @SuppressWarnings("unchecked")
    public static void setProperty(Node node, String propertyName, Object propertyValue) throws RepositoryException {
        if (node == null) {
            throw new IllegalArgumentException("Cannot set a property on a null-node!");
        }
        if (propertyName == null) {
            throw new IllegalArgumentException("Cannot set a property without a provided name");
        }

        // let's find out what type of value we got
        if (propertyValue instanceof Value) {
            node.setProperty(propertyName, (Value) propertyValue);
        } else if (propertyValue instanceof Node) {
            node.setProperty(propertyName, (Node) propertyValue);
        } else if (propertyValue instanceof Binary) {
            node.setProperty(propertyName, (Binary) propertyValue);
        } else if (propertyValue instanceof Calendar) {
            node.setProperty(propertyName, (Calendar) propertyValue);
        } else if (propertyValue instanceof Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) propertyValue);
            node.setProperty(propertyName, cal);
        } else if (propertyValue instanceof BigDecimal) {
            node.setProperty(propertyName, (BigDecimal) propertyValue);
        } else if (propertyValue instanceof String) {
            node.setProperty(propertyName, (String) propertyValue);
        } else if (propertyValue instanceof Long) {
            node.setProperty(propertyName, ((Long) propertyValue).longValue());
        } else if (propertyValue instanceof Double) {
            node.setProperty(propertyName, (Double) propertyValue);
        } else if (propertyValue instanceof Boolean) {
            node.setProperty(propertyName, (Boolean) propertyValue);
        } else if (propertyValue instanceof InputStream) {
            node.setProperty(propertyName, (InputStream) propertyValue);
        } else if (propertyValue instanceof Collection) {
            String[] list = new String[((Collection<Object>)propertyValue).size()];
            int pos = 0;
            for (Object value : (Collection<Object>)propertyValue) {
                list[pos] = value.toString();
                pos +=1;
            }
            node.setProperty(propertyName, list);
        } else {
            // TODO dlipp: verify if this is desired default-behavior: NodeDataUtil#setValue sets propertyValue.toString() as default!
            throw new IllegalArgumentException("Cannot set property to a value of type " + propertyValue.getClass());
        }
    }

    /**
     * Transforms a string to a jcr value object.
     */
    public static Value createValue(String valueStr, int type, ValueFactory valueFactory) {
        Value value = null;
        if (type == PropertyType.STRING) {
            value = valueFactory.createValue(valueStr);
        } else if (type == PropertyType.BOOLEAN) {
            value = valueFactory.createValue(BooleanUtils.toBoolean(valueStr));
        } else if (type == PropertyType.DOUBLE) {
            try {
                value = valueFactory.createValue(Double.parseDouble(valueStr));
            } catch (NumberFormatException e) {
                value = valueFactory.createValue(0d);
            }
        } else if (type == PropertyType.LONG) {
            try {
                value = valueFactory.createValue(Long.parseLong(valueStr));
            } catch (NumberFormatException e) {
                value = valueFactory.createValue(0L);
            }
        } else if (type == PropertyType.DATE) {
            try {
                Calendar date = new GregorianCalendar();
                try {
                    String newDateAndTime = valueStr;
                    String[] dateAndTimeTokens = newDateAndTime.split("T");
                    String newDate = dateAndTimeTokens[0];
                    String[] dateTokens = newDate.split("-");
                    int hour = 0;
                    int minute = 0;
                    int second = 0;
                    int year = Integer.parseInt(dateTokens[0]);
                    int month = Integer.parseInt(dateTokens[1]) - 1;
                    int day = Integer.parseInt(dateTokens[2]);
                    if (dateAndTimeTokens.length > 1) {
                        String newTime = dateAndTimeTokens[1];
                        String[] timeTokens = newTime.split(":");
                        hour = Integer.parseInt(timeTokens[0]);
                        minute = Integer.parseInt(timeTokens[1]);
                        second = Integer.parseInt(timeTokens[2]);
                    }
                    date.set(year, month, day, hour, minute, second);
                    // this is used in the searching
                    date.set(Calendar.MILLISECOND, 0);
                    date.setTimeZone(TimeZone.getTimeZone("GMT"));
                }
                // todo time zone??
                catch (Exception e) {
                    // ignore, it sets the current date / time
                }
                value = valueFactory.createValue(date);
            } catch (Exception e) {
                log.debug("Exception caught: " + e.getMessage(), e);
            }
        }

        return value;

    }

    /**
     * @return JCR-PropertyType corresponding to provided Object.
     */
    public static int getJCRPropertyType(Object obj) {
        if (obj instanceof String) {
            return PropertyType.STRING;
        }
        if (obj instanceof Double) {
            return PropertyType.DOUBLE;
        }
        if (obj instanceof Float) {
            return PropertyType.DOUBLE;
        }
        if (obj instanceof Long) {
            return PropertyType.LONG;
        }
        if (obj instanceof Integer) {
            return PropertyType.LONG;
        }
        if (obj instanceof Boolean) {
            return PropertyType.BOOLEAN;
        }
        if (obj instanceof Calendar) {
            return PropertyType.DATE;
        }
        if (obj instanceof Binary) {
            return PropertyType.BINARY;
        }
        if (obj instanceof InputStream) {
            return PropertyType.BINARY;
        }
        if (obj instanceof Content) {
            return PropertyType.REFERENCE;
        }
        return PropertyType.UNDEFINED;
    }

    /**
     * Updates existing property or creates a new one if it doesn't exist already.
     */
    public static void updateOrCreate(Node node, String string, GregorianCalendar gregorianCalendar) throws RepositoryException {
        if (node.hasProperty(string)) {
            node.getProperty(string).setValue(gregorianCalendar);
        } else {
            node.setProperty(string, gregorianCalendar);
        }
    }

    public static String getDateFormat() {
        try {
            return FastDateFormat.getDateInstance(
                    FastDateFormat.SHORT,
                    MgnlContext.getLocale()).getPattern();
        } catch (IllegalStateException e) {
            // this happens if the context is not (yet) set
            return DateUtil.YYYY_MM_DD;
        }
    }

    public static List<String> getValuesStringList(Value[] values) {
        ArrayList<String> list = new ArrayList<String>();
        for (Value value : values) {
            list.add(getValueString(value));
        }
        return list;
    }

    /**
     * Returns value of the property converted to string no matter what it's type actually is. In case of dates, value if formatted according to format returned by {@link #getDateFormat()}. Binary and reference values are converted to empty string. In case of error during conversion, null will be returned instead. Works only for single value properties.
     */
    public static String getValueString(Property property) {
        try {
            return getValueString(property.getValue());
        } catch (RepositoryException e) {
            log.debug("RepositoryException caught: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns value converted to string no matter what it's type actually is. In case of dates, value if formatted according to format returned by {@link #getDateFormat()}. Binary and reference values are converted to empty string. In case of error during conversion, null will be returned instead.
     */
    public static String getValueString(Value value) {
        try {
            switch (value.getType()) {
            case (PropertyType.STRING):
                return value.getString();
            case (PropertyType.DOUBLE):
                return Double.toString(value.getDouble());
            case (PropertyType.LONG):
                return Long.toString(value.getLong());
            case (PropertyType.BOOLEAN):
                return Boolean.toString(value.getBoolean());
            case (PropertyType.DATE):
                Date valueDate = value.getDate().getTime();
            return DateUtil.format(valueDate, PropertyUtil.getDateFormat());
            case (PropertyType.BINARY):
                // for lack of better solution, fall through to the default - empty string
            default:
                return StringUtils.EMPTY;
            }
        } catch (RepositoryException e) {
            log.debug("RepositoryException caught: " + e.getMessage(), e);
        }
        return null;

    }

    public static Value createValue(Object obj, ValueFactory valueFactory) throws RepositoryException {
        switch (PropertyUtil.getJCRPropertyType(obj)) {
        case PropertyType.STRING:
            return valueFactory.createValue((String) obj);
        case PropertyType.BOOLEAN:
            return valueFactory.createValue((Boolean) obj);
        case PropertyType.DATE:
            return valueFactory.createValue((Calendar) obj);
        case PropertyType.LONG:
            return obj instanceof Long ? valueFactory.createValue(((Long) obj).longValue()) : valueFactory.createValue(((Integer) obj).longValue());
        case PropertyType.DOUBLE:
            return obj instanceof Double ? valueFactory.createValue((Double) obj) : valueFactory.createValue(((Float) obj).doubleValue());
        case PropertyType.BINARY:
            return valueFactory.createValue((InputStream) obj);
        case PropertyType.REFERENCE:
            return valueFactory.createValue(((Content) obj).getJCRNode());
        default:
            return (obj != null ? valueFactory.createValue(obj.toString()) : valueFactory.createValue(StringUtils.EMPTY));
        }
    }

    /**
     * Return the Calendar representing the node property value.
     * If the Node did not contain such a Property,
     * then return <b>null</b>.
     */
    public static Calendar getDate(Node node, String name) {
        return getDate(node, name, null);
    }

    /**
     * Return the Calendar representing the node property value.
     * If the Node did not contain such a Property,
     * then return the default value.
     */
    public static Calendar getDate(Node node, String name, Calendar defaultValue) {
        try {
            if (node.hasProperty(name)) {
                return node.getProperty(name).getDate();
            }
        } catch (RepositoryException e) {
            log.error("can't read value '" + name + "' of the Node '" + node.toString() + "' will return default value", e);
        }
        return defaultValue;
    }

    /**
     * Return the String representing the node property value.
     * If the Node did not contain such a Property,
     * then return <b>null</b>.
     */
    public static String getString(Node node, String name) {
        return getString(node, name, null);
    }

    /**
     * Return the String representing the node property value.
     * If the Node did not contain such a Property,
     * then return the default value.
     */
    public static String getString(Node node, String name, String defaultValue) {
        try {
            if (node.hasProperty(name)) {
                return node.getProperty(name).getString();
            }
        } catch (RepositoryException e) {
            log.error("can't read value '" + name + "' of the Node '" + node.toString() + "' will return default value", e);
        }
        return defaultValue;
    }

    /**
     * Return the boolean representing the node property value.
     * If the Node did not contain such a Property,
     * then return the default value.
     */
    public static boolean getBoolean(Node node, String name, boolean defaultValue) {
        try {
            if (node.hasProperty(name)) {
                return node.getProperty(name).getBoolean();
            }
        } catch (RepositoryException e) {
            log.error("can't read value '" + name + "' of the Node '" + node.toString() + "' will return default value", e);
        }
        return defaultValue;
    }

    /**
     * Return the Property relative to the Node.
     * Return null in case of Exception.
     */
    public static Property getProperty(Node node, String relativePath) {
        try {
            return node.getProperty(relativePath);
        }
        catch (PathNotFoundException e) {
            log.error("Property Access Exception ",e);
        }
        catch (RepositoryException e) {
            log.error("Property Access Exception ",e);
        }
        return null;
    }

    /**
     * Return the Value Object from a property.
     * Return null in case of exception.
     * The returned Object could be a basic {@link PropertyType} type or in case
     * of multivalue, a Set of {@link PropertyType} type objects.
     *
     */
    public static Object getPropertyValueObject(Node node, String relativePath) {
        Property property = getProperty(node, relativePath);
        if(property != null) {
            try {
                //Handle Multivalue fields
                if(property.isMultiple()) {
                    Value[] values = property.getValues();
                    Set<Object> res = new HashSet<Object>();
                    for(Value value:values) {
                        res.add(getValueObject(value));
                    }
                    return res;
                } else {
                    return getValueObject(property.getValue());
                }

            } catch (Exception e) {
                log.warn("Exception during casting the property value", e);
            }
        }
        return null;
    }

    /**
     * Return the Value Object from a {@link Value}.
     * Return null in case of exception.
     */
    public static Object getValueObject(Value value) {
        try {
            switch (value.getType()) {
                case (PropertyType.DECIMAL):
                    return value.getDecimal();
                case (PropertyType.STRING):
                    return value.getString();
                case (PropertyType.DOUBLE):
                    return Double.valueOf(value.getDouble());
                case (PropertyType.LONG):
                    return Long.valueOf(value.getLong());
                case (PropertyType.BOOLEAN):
                    return Boolean.valueOf(value.getBoolean());
                case (PropertyType.DATE):
                    return value.getDate().getTime();
                case (PropertyType.BINARY):
                    return IOUtils.toByteArray(value.getBinary().getStream());
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Exception during casting the property value", e);
        }
        return null;
    }

}
