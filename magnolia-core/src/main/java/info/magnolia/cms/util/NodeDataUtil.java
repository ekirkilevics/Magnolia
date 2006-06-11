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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;

import java.util.Date;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class NodeDataUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(NodeDataUtil.class);

    NodeData nodeData;

    public NodeDataUtil(NodeData nodeData) {
        this.setNodeData(nodeData);
    }

    public void setNodeData(NodeData nodeData) {
        this.nodeData = nodeData;
    }

    public NodeData getNodeData() {
        return this.nodeData;
    }

    /**
     * <p/> Returns the representation of the value as a String:
     * </p>
     * @return String
     */
    public String getValueString() {
        return getValueString(null);
    }

    /**
     * <p/> Returns the representation of the value as a String:
     * </p>
     * @return String
     */
    public String getValueString(String dateFormat) {
        try {
            NodeData nodeData = this.getNodeData();
            switch (nodeData.getType()) {
                case (PropertyType.STRING):
                    return nodeData.getString();
                case (PropertyType.DOUBLE):
                    return Double.toString(nodeData.getDouble());
                case (PropertyType.LONG):
                    return Long.toString(nodeData.getLong());
                case (PropertyType.BOOLEAN):
                    return Boolean.toString(nodeData.getBoolean());
                case (PropertyType.DATE):
                    Date valueDate = nodeData.getDate().getTime();
                    return new DateUtil().getFormattedDate(valueDate, dateFormat);
                case (PropertyType.BINARY):
                    // ???
                default:
                    return StringUtils.EMPTY;
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the value as an Object.
     * @return Object
     */
    public static Object getValue(NodeData nd) {
        try {
            switch (nd.getType()) {
                case (PropertyType.STRING):
                    return nd.getString();
                case (PropertyType.DOUBLE):
                    return new Double(nd.getDouble());
                case (PropertyType.LONG):
                    return new Long(nd.getLong());
                case (PropertyType.BOOLEAN):
                    return new Boolean(nd.getBoolean());
                case (PropertyType.DATE):
                    return nd.getDate().getTime();
                case (PropertyType.BINARY):
                    return null;
                default:
                    return null;
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return null;
    }

    public String getTypeName(int type) {

        switch (type) {
            case PropertyType.STRING:
                return PropertyType.TYPENAME_STRING;
            case PropertyType.BOOLEAN:
                return PropertyType.TYPENAME_BOOLEAN;
            case PropertyType.DATE:
                return PropertyType.TYPENAME_DATE;
            case PropertyType.LONG:
                return PropertyType.TYPENAME_LONG;
            case PropertyType.DOUBLE:
                return PropertyType.TYPENAME_DOUBLE;
            case PropertyType.BINARY:
                return PropertyType.TYPENAME_BINARY;
            default:
                return StringUtils.EMPTY;
        }

    }

    /**
     * Simple method to get strings like configuration informations
     * @param repository
     * @param path
     * @return
     */
    public static String getString(String repository, String path) {
        return getString(repository, path, null);
    }

    /**
     * Get the string or the empty string if not existing
     * @param node
     * @param name
     * @return a string
     */
    public static String getString(Content node, String name) {
        return getString(node, name, "");
    }

    /**
     * You can define a default value if not found
     * @param repository
     * @param path
     * @param defaultValue
     * @return the string
     */
    public static String getString(String repository, String path, String defaultValue) {
        try {
            return MgnlContext.getHierarchyManager(repository).getNodeData(path).getString();
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * You can define a default value if not found
     * @param node
     * @param name
     * @param defaultValue
     * @return the string
     */
    public static String getString(Content node, String name, String defaultValue) {
        try {
            if (node.hasNodeData(name)) {
                return node.getNodeData(name).getString();
            }

            return defaultValue;
        }
        catch (Exception e) {
            return defaultValue;
        }
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

    /**
     * Uses the i18n mechanism to translate the message if the resulting string is a key
     * @param node
     * @param string
     * @return the i18n string
     */
    public static Object getI18NString(Content node, String str) {
        String key = getString(node, str);
        String i18nBasename = NodeDataUtil.getString(node, "i18nBasename");
        if (StringUtils.isNotEmpty(i18nBasename)) {
            Messages msgs = MessagesUtil.chainWithDefault(i18nBasename);
            return msgs.getWithDefault(key, key);
        }

        return MessagesManager.getWithDefault(key, key);

    }

    /**
     * Uses the i18n mechanism to translate the message if the resulting string is a key
     */
    public static Object getI18NString(Content node, String str, String basename) {
        String key = getString(node, str);
        return MessagesManager.getMessages(basename).getWithDefault(key, key);
    }

}
