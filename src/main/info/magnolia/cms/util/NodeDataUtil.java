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
package info.magnolia.cms.util;

import info.magnolia.cms.core.NodeData;

import java.util.Date;

import javax.jcr.PropertyType;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public class NodeDataUtil {

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
     * <p>
     * Returns the representation of the value as a String:
     * </p>
     * @return String
     */
    public String getValueString() {
        return getValueString(null);
    }

    /**
     * <p>
     * Returns the representation of the value as a String:
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
        }
        return StringUtils.EMPTY;
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
}
