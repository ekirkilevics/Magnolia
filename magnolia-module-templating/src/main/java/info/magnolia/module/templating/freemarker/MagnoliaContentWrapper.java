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
package info.magnolia.module.templating.freemarker;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import javax.jcr.PropertyType;
import java.util.Calendar;

/**
 * @author Chris Miner
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaContentWrapper extends DefaultObjectWrapper {

    MagnoliaContentWrapper() {
        super();
    }

    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof NodeData) {
            final NodeData nodeData = (NodeData) obj;
            switch (nodeData.getType()) {
                case PropertyType.BOOLEAN:
                    return nodeData.getBoolean() ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;

                case PropertyType.DATE:
                    return handleCalendar(nodeData.getDate());

                case PropertyType.DOUBLE:
                    return new SimpleNumber(nodeData.getDouble());

                case PropertyType.LONG:
                    return new SimpleNumber(nodeData.getLong());

                case PropertyType.STRING:
                    return new SimpleScalar(nodeData.getString());

                case PropertyType.BINARY:
                    return new BinaryNodeData(nodeData, this);

//                case PropertyType.REFERENCE:
//                case PropertyType.PATH:
//                case PropertyType.NAME:
                default:
                    throw new TemplateModelException("Unsupported property type: " + PropertyType.nameFromValue(nodeData.getType()));
            }
        } else if (obj instanceof Content) {
            return new ContentModel((Content) obj, this);
        } else if (obj instanceof Calendar) { // this is needed ie. for MetaData dates
            return handleCalendar((Calendar) obj);
        } else {
            return super.wrap(obj);
        }
    }

    protected SimpleDate handleCalendar(Calendar cal) {
        return new SimpleDate(cal.getTime(), TemplateDateModel.DATETIME);
    }

}