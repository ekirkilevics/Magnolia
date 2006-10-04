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
package info.magnolia.cms.gui.controlx.list;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import java.util.Calendar;
import java.util.Date;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles $Id$
 */
public class DefaultValueProvider implements ValueProvider {

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(DefaultValueProvider.class);

    /**
     * singleton
     */
    private static ValueProvider thisInstance = new DefaultValueProvider();

    /**
     * Not allowed to be instanciated outside the scope of this class
     */
    protected DefaultValueProvider() {
    }

    /**
     * get instance
     */
    public static ValueProvider getInstance() {
        return thisInstance;
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.list.util.ValueProvider#getValue(java.lang.String, java.lang.Object)
     */
    public Object getValue(String name, Object obj) {
        Object value = null;
        try {
            if (obj instanceof Content) {
                Content node = (Content) obj;
                if (node.hasNodeData(name)) {
                    NodeData nd = node.getNodeData(name);
                    if (nd.getType() == PropertyType.DATE) {
                        value = nd.getDate();
                    }
                    else {
                        value = nd.getString();
                    }
                }

                if (value == null) {
                    try {
                        value = PropertyUtils.getProperty(node.getMetaData(), name);
                    }
                    catch (NoSuchMethodException e) {
                        value = node.getMetaData().getStringProperty(name);
                        if (StringUtils.isEmpty((String) value)) {
                            value = null;
                        }
                    }
                }
            }

            if (value == null) {
                // is this a property of the object
                try {
                    value = PropertyUtils.getProperty(obj, name);
                }
                catch (NoSuchMethodException e1) {
                    // check if getter exist for this name
                    try {
                        String methodName = "get"
                            + StringUtils.substring(name, 0, 1).toUpperCase()
                            + StringUtils.substring(name, 1);
                        value = MethodUtils.invokeMethod(this, methodName, obj);
                    }
                    catch (NoSuchMethodException e2) {
                        value = StringUtils.EMPTY;
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("can't get value", e);
            value = StringUtils.EMPTY;
        }

        if (value instanceof Calendar) {
            value = new Date(((Calendar) value).getTimeInMillis());
        }

        return value;
    }

    /**
     * get node type
     * @return node type
     */
    public String getType(Content node) {
        try {
            return node.getNodeTypeName();
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
