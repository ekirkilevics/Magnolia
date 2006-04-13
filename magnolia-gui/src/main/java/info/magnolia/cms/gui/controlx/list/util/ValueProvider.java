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
package info.magnolia.cms.gui.controlx.list.util;

import info.magnolia.cms.core.Content;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author Sameer Charles $Id$
 */
public class ValueProvider {

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(ValueProvider.class);

    /**
     * singleton
     */
    private static ValueProvider thisInstance = new ValueProvider();

    /**
     * Not allowed to be instanciated outside the scope of this class
     */
    private ValueProvider() {
    }

    /**
     * get instance
     */
    public static ValueProvider getInstance() {
        return thisInstance;
    }

    /**
     * get value - first check for property in this object - then look for the getter for this name - else search in
     * MetaData
     * @param name
     * @param node
     */
    public synchronized Object getValue(String name, Object obj) {
        try {
            if (obj instanceof Content) {
                Content node = (Content) obj;
                if (node.hasNodeData(name)) {
                    return node.getNodeData(name).getString();
                }

                String value = node.getMetaData().getStringProperty(name);
                if (StringUtils.isNotEmpty(value)) {
                    return value;
                }
            }

            // is this a property of the object
            try {
                return PropertyUtils.getProperty(obj, name);
            }
            catch (NoSuchMethodException e1) {
                // check if getter exist for this name
                try {
                    String methodName = "get"
                        + StringUtils.substring(name, 0, 1).toUpperCase()
                        + StringUtils.substring(name, 1);
                    return MethodUtils.invokeMethod(this, methodName, obj);
                }
                catch (NoSuchMethodException e2) {
                    return StringUtils.EMPTY;
                }
            }
        }
        catch (Exception e) {
            log.error("can't get value", e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * get node type
     * @return node type
     */
    public String getType(Content node) {
        try {
            return node.getNodeType().getName();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return StringUtils.EMPTY;
    }

    /**
     * get path
     * @return handle for the ciurrent object
     */
    public String getPath(Content node) {
        return node.getHandle();
    }

}
