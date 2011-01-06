/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util to work with {@link NodeData}.
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class NodeDataUtil {
    private static final Logger log = LoggerFactory.getLogger(NodeDataUtil.class);

    /**
     * Same as getValueString(nd, dateFormat) but using the users short date format.
     */
    public static String getValueString(NodeData nodeData) {
        String dateFormat = null;
        if(nodeData.getType() == PropertyType.DATE){
            try{
                dateFormat = FastDateFormat.getDateInstance(
                FastDateFormat.SHORT,
                MgnlContext.getLocale()).getPattern();
            }
            // this happens if the context is not (yet) set
            catch(IllegalStateException e){
                dateFormat = DateUtil.YYYY_MM_DD;
            }
        }
        return getValueString(nodeData, dateFormat);
    }

    /**
     * Returns a String representation of the value. In case of a binary the path including filename and extension is returned
     * @param nodeData
     * @param dateFormat date format to use in the case it is a date
     * @return
     */
    public static String getValueString(NodeData nodeData, String dateFormat) {
        // we can't use FileProperties since this class is in the GUI package
        if(nodeData.getType()==PropertyType.BINARY){
            String filename = nodeData.getAttribute("fileName");
            String ext = nodeData.getAttribute("extension");
            String fullName = filename;
            String fullExt = StringUtils.EMPTY;
            if (StringUtils.isNotEmpty(ext)) {
                fullExt = "." + ext;
                fullName += fullExt;
            }
            return nodeData.getHandle() + "/" + fullName;
        }
        else if (nodeData.isMultiValue() == NodeData.MULTIVALUE_TRUE){
            return StringUtils.join(getValuesStringList(nodeData.getValues()), ",");
        } else {
            return getValueString(nodeData.getValue(), dateFormat);
        }
    }

    /**
     * Same as value.getString(), but using custom date format.
     */
    public static String getValueString(Value value, String dateFormat) {
        try{
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
                    return DateUtil.format(valueDate, dateFormat);
                case (PropertyType.BINARY):
                    // ???
                default:
                    return StringUtils.EMPTY;
            }
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

    /**
     * Inherit a value. Uses the string value. The "inherit" means that the method will look for the value in the content itself and if not found
     * it will go up in the tree and try to locate value in one of the parents of the content until reaching the root. The first value found while
     * traversing the tree way up is the one that will be returned.
     * @param node Node expected to define or inherit the searched node value.
     * @param name Name of the nodeData.
     */
    public static String inheritString(Content node, String name) throws RepositoryException{
        String value = "";

        if (node.hasNodeData(name)) {
            value = NodeDataUtil.getString(node, name);
        }
        if(StringUtils.isEmpty(value) && node.getLevel() > 0){
            value = inheritString(node.getParent(), name);
        }
        return value;
    }

    /**
     * Inherit a value. You can provide a default value if not found
     */
    public static String inheritString(Content node, String name, String dflt) throws RepositoryException{
        String value = inheritString(node, name);
        if(StringUtils.isEmpty(value)){
            return dflt;
        }
        return value;
    }

    /**
     * Inherit a value. Uses the string value
     */
    public static Object inherit(Content node, String name) throws RepositoryException{
        Object value = null;

        if (node.hasNodeData(name)) {
            value = NodeDataUtil.getValueObject(node.getNodeData(name));
        }
        if(value == null && node.getLevel() > 0){
            value = inherit(node.getParent(), name);
        }
        return value;
    }

    /**
     * Inherit a value. You can provide a default value if not found
     */
    public static Object inherit(Content node, String name, Object dflt) throws RepositoryException{
        Object value = inherit(node, name);
        if(value == null){
            return dflt;
        }
        return value;
    }

    /**
     * Returns the value as an Object.
     * @return Object
     */
    public static Object getValueObject(NodeData nd) {
        try {
            switch (nd.getType()) {
                case (PropertyType.STRING):
                    return nd.getString();
                case (PropertyType.DOUBLE):
                    return new Double(nd.getDouble());
                case (PropertyType.LONG):
                    return new Long(nd.getLong());
                case (PropertyType.BOOLEAN):
                    return Boolean.valueOf(nd.getBoolean());
                case (PropertyType.DATE):
                    return nd.getDate().getTime();
                case (PropertyType.BINARY):
                    return null;
                default:
                    return null;
            }
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Calls the correct setValue method based on object type. If the value is null an empty string is set.
     */
    public static NodeData setValue(NodeData nodeData, Object valueObj) throws AccessDeniedException, RepositoryException{
        if(valueObj == null){
            nodeData.setValue(StringUtils.EMPTY);
        }
        else{
            switch (getJCRPropertyType(valueObj)) {
                case PropertyType.STRING:
                    nodeData.setValue((String)valueObj);
                    break;
                case PropertyType.BOOLEAN:
                    nodeData.setValue(((Boolean)valueObj).booleanValue());
                    break;
                case PropertyType.DATE:
                    nodeData.setValue((Calendar)valueObj);
                    break;
                case PropertyType.LONG:
                    nodeData.setValue(((Long)valueObj).longValue());
                    break;
                case PropertyType.DOUBLE:
                    nodeData.setValue(((Double)valueObj).doubleValue());
                    break;
                case PropertyType.BINARY:
                    nodeData.setValue((InputStream)valueObj);
                    break;
                case PropertyType.REFERENCE:
                    nodeData.setValue((Content)valueObj);
                    break;
                default:
                    nodeData.setValue(valueObj.toString());
            }
        }
        return nodeData;
    }


    /**
     * String representation of the jcr property type.
     */
    public static String getTypeName(NodeData nd) {
        return PropertyType.nameFromValue(nd.getType());
    }

    /**
     * Simple method to get strings like configuration informations.
     */
    public static String getString(String repository, String path) {
        return getString(repository, path, null);
    }

    /**
     * Get the string or the empty string if not existing.
     */
    public static String getString(Content node, String name) {
        return getString(node, name, "");
    }

    /**
     * You can define a default value if not found.
     */
    public static String getString(String repository, String path, String defaultValue) {
        try {
            String name = StringUtils.substringAfterLast(path, "/");
            String nodeHandle = StringUtils.substringBeforeLast(path, "/");
            Content node = MgnlContext.getHierarchyManager(repository).getContent(nodeHandle);
            return getString(node, name);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * You can define a default value if not found.
     */
    public static String getString(Content node, String name, String defaultValue) {
        try {
            if (node.hasNodeData(name)) {
                return getValueString(node.getNodeData(name));
            }

            return defaultValue;
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getLong(Content node, String name, long defaultValue) {
        try {
            if(node.hasNodeData(name)){
                return node.getNodeData(name).getLong();
            }
        }
        // should not happen
        catch (RepositoryException e) {
            log.error("can't read value will return default value", e);
        }
        return defaultValue;
    }

    public static Calendar getDate(Content node, String name, Calendar defaultValue) {
        try {
            if(node.hasNodeData(name)){
                return node.getNodeData(name).getDate();
            }
        }
        // should not happen
        catch (RepositoryException e) {
            log.error("can't read value will return default value", e);
        }
        return defaultValue;
    }

    public static boolean getBoolean(Content node, String name, boolean defaultValue) {
        try {
            if(node.hasNodeData(name)){
                return node.getNodeData(name).getBoolean();
            }
        }
        // should not happen
        catch (RepositoryException e) {
            log.error("can't read value will return default value", e);
        }
        return defaultValue;
    }

    /**
     * If the NodeData does not exist yet, just create it.
     * @param node
     * @param name
     * @return the found or created NodeData
     * @throws AccessDeniedException
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static NodeData getOrCreate(Content node, String name) throws AccessDeniedException, RepositoryException {
        return getOrCreate(node, name, PropertyType.STRING);
    }

    /**
     * If the NodeData does not exist yet, just create it.
     * @param node
     * @param name
     * @return the found or created NodeData
     * @throws AccessDeniedException
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static NodeData getOrCreate(Content node, String name, int type) throws AccessDeniedException,
        RepositoryException {
        if (node.hasNodeData(name)) {
            return node.getNodeData(name);
        }

        return node.createNodeData(name, type);
    }

    public static NodeData getOrCreate(Content node, String name, Object obj) throws AccessDeniedException,
        RepositoryException {

        return getOrCreate(node, name, getJCRPropertyType(obj));
    }


    public static NodeData getOrCreateAndSet(Content node, String name, Object obj) throws AccessDeniedException, RepositoryException {
        // TODO we should not use the jcr node
        ValueFactory valueFactory = node.getJCRNode().getSession().getValueFactory();
        NodeData nd = getOrCreate(node, name, getJCRPropertyType(obj));
        nd.setValue(createValue(obj, valueFactory));
        return nd;
    }

    public static NodeData getOrCreateAndSet(Content node, String name, long value) throws AccessDeniedException, RepositoryException {
        return getOrCreateAndSet(node, name, new Long(value));
    }

    public static NodeData getOrCreateAndSet(Content node, String name, Value[] value) throws AccessDeniedException, RepositoryException {
        if (node.hasNodeData(name)) {
            node.setNodeData(name, value);
            return node.getNodeData(name);
        }

        return node.createNodeData(name, value);

    }

    public static NodeData getOrCreateAndSet(Content node, String name, int value) throws AccessDeniedException, RepositoryException {
        return getOrCreateAndSet(node, name, new Long(value));
    }

    public static NodeData getOrCreateAndSet(Content node, String name, boolean value) throws AccessDeniedException, RepositoryException {
        return getOrCreateAndSet(node, name, new Boolean(value));
    }

    /**
     * Uses the i18n mechanism to translate the message if the resulting string is a key.
     */
    public static String getI18NString(Content node, String str) {
        Messages msgs = MessagesManager.getMessages();
        String key = getString(node, str);
        String i18nBasename = null;
        try {
            i18nBasename = NodeDataUtil.inheritString(node, "i18nBasename");
        }
        catch (RepositoryException e) {
            log.error("can't read i18nBasename value, will default back", e);
        }

        if (StringUtils.isNotEmpty(i18nBasename)) {
            msgs = MessagesUtil.chainWithDefault(i18nBasename);
        }

        return msgs.getWithDefault(key, key);
    }

    /**
     * Uses the i18n mechanism to translate the message if the resulting string is a key.
     */
    public static String getI18NString(Content node, String str, String basename) {
        String key = getString(node, str);
        return MessagesManager.getMessages(basename).getWithDefault(key, key);
    }

    /**
     * Uses the default value factory.
     */
    public static Value createValue(String valueStr, int type) throws RepositoryException {
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
        ValueFactory valueFactory = hm.getWorkspace().getSession().getValueFactory();
        return createValue(valueStr, type, valueFactory);
    }

    public static Value createValue(Object obj, ValueFactory valueFactory) throws RepositoryException {
        switch (getJCRPropertyType(obj)) {
            case PropertyType.STRING:
                return valueFactory.createValue((String)obj);
            case PropertyType.BOOLEAN:
                return valueFactory.createValue(((Boolean)obj).booleanValue());
            case PropertyType.DATE:
                return valueFactory.createValue((Calendar)obj);
            case PropertyType.LONG:
                return valueFactory.createValue(((Long)obj).longValue());
            case PropertyType.DOUBLE:
                return valueFactory.createValue(((Double)obj).doubleValue());
            case PropertyType.BINARY:
                return valueFactory.createValue((InputStream)obj);
            case PropertyType.REFERENCE:
                return valueFactory.createValue(((Content)obj).getJCRNode());
            default:
                return (obj != null ? valueFactory.createValue(obj.toString()) : valueFactory.createValue(StringUtils.EMPTY));
        }
    }

    /**
     * Transforms a string to a jcr value object.
     */
    public static Value createValue(String valueStr, int type, ValueFactory valueFactory) {
        Value value = null;
        if (type == PropertyType.STRING) {
            value = valueFactory.createValue(valueStr);
        }
        else if (type == PropertyType.BOOLEAN) {
            value = valueFactory.createValue(BooleanUtils.toBoolean(valueStr));
        }
        else if (type == PropertyType.DOUBLE) {
            try {
                value = valueFactory.createValue(Double.parseDouble(valueStr));
            }
            catch (NumberFormatException e) {
                value = valueFactory.createValue(0d);
            }
        }
        else if (type == PropertyType.LONG) {
            try {
                value = valueFactory.createValue(Long.parseLong(valueStr));
            }
            catch (NumberFormatException e) {
                value = valueFactory.createValue(0L);
            }
        }
        else if (type == PropertyType.DATE) {
            try {
                Calendar date = new GregorianCalendar();
                try {
                    String newDateAndTime = valueStr;
                    String[] dateAndTimeTokens = newDateAndTime.split("T"); //$NON-NLS-1$
                    String newDate = dateAndTimeTokens[0];
                    String[] dateTokens = newDate.split("-"); //$NON-NLS-1$
                    int hour = 0;
                    int minute = 0;
                    int second = 0;
                    int year = Integer.parseInt(dateTokens[0]);
                    int month = Integer.parseInt(dateTokens[1]) - 1;
                    int day = Integer.parseInt(dateTokens[2]);
                    if (dateAndTimeTokens.length > 1) {
                        String newTime = dateAndTimeTokens[1];
                        String[] timeTokens = newTime.split(":"); //$NON-NLS-1$
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
            }
            catch (Exception e) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }

        return value;

    }

    public static int getJCRPropertyType(Object obj) {
        if(obj instanceof String) {
            return PropertyType.STRING;
        }
        if(obj instanceof Double) {
            return PropertyType.DOUBLE;
        }
        if(obj instanceof Long) {
            return PropertyType.LONG;
        }
        if(obj instanceof Integer) {
            return PropertyType.LONG;
        }
        if(obj instanceof Boolean) {
            return PropertyType.BOOLEAN;
        }
        if(obj instanceof Calendar) {
            return PropertyType.DATE;
        }
        if(obj instanceof InputStream) {
            return PropertyType.BINARY;
        }
        if(obj instanceof Content) {
            return PropertyType.REFERENCE;
        }
        return PropertyType.UNDEFINED;
    }

    public static List<String> getValuesStringList(Value[] values) {
        ArrayList<String> list = new ArrayList<String>();
        try{
            Value value;
            for( int i = 0; i < values.length; i++) {
                value = values[i];
                switch (value.getType()) {
                    case (PropertyType.STRING):
                        list.add(value.getString());
                        break;
                    case (PropertyType.DOUBLE):
                        list.add(Double.toString(value.getDouble()));
                        break;
                    case (PropertyType.LONG):
                        list.add(Long.toString(value.getLong()));
                        break;
                    case (PropertyType.BOOLEAN):
                        list.add(Boolean.toString(value.getBoolean()));
                        break;
                    case (PropertyType.DATE):
                        Date valueDate = value.getDate().getTime();
                        list.add(DateUtil.format(valueDate, NodeDataUtil.getDateFormat()));
                        break;
                    case (PropertyType.BINARY):
                        // for lack of better solution, fall through to the default - empty string
                    default:
                        list.add(StringUtils.EMPTY);
                }
            }
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return list;
    }

    public static String getDateFormat() {
        String dateFormat = null;
        try{
            dateFormat = FastDateFormat.getDateInstance(
            FastDateFormat.SHORT,
            MgnlContext.getLocale()).getPattern();
        }
        // this happens if the context is not (yet) set
        catch(IllegalStateException e){
            dateFormat = DateUtil.YYYY_MM_DD;
        }
        return dateFormat;
    }
}
