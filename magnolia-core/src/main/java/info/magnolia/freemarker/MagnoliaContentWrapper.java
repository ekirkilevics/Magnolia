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
package info.magnolia.freemarker;

import freemarker.ext.beans.BeanModel;
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
import info.magnolia.cms.link.AbsolutePathTransformer;
import info.magnolia.cms.link.CompleteUrlPathTransformer;
import info.magnolia.cms.link.PathToLinkTransformer;
import info.magnolia.cms.link.RelativePathTransformer;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.Calendar;

/**
 * @author Chris Miner
 * @author gjoseph
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
                    final String s = nodeData.getString();
                    final PathToLinkTransformer t;
                    // TODO : maybe this could be moved to LinkUtil
                    if (MgnlContext.getInstance() instanceof WebContext) {
                        final Content page = MgnlContext.getAggregationState().getMainContent();
                        if (page != null) {
                            t = new RelativePathTransformer(page, true, true);
                        } else {
                            t = new AbsolutePathTransformer(true, true, true);
                        }
                    } else {
                        t = new CompleteUrlPathTransformer(true, true);
                    }
                    final String transformedString = LinkUtil.convertUUIDsToLinks(s, t);
                    return new SimpleScalar(transformedString);

                case PropertyType.BINARY:
                    return new BinaryNodeData(nodeData, this);

                case PropertyType.REFERENCE:
                    try {
                        Content c = nodeData.getReferencedContent();
                        return new ContentModel(c, this);
                    } catch (RepositoryException e) {
                        throw new TemplateModelException(e);
                    }

//                case PropertyType.PATH:
//                case PropertyType.NAME:
                default:
                    throw new TemplateModelException("Unsupported property type: " + PropertyType.nameFromValue(nodeData.getType()));
            }
        } else if (obj instanceof Content) {
            return new ContentModel((Content) obj, this);
        } else if (obj instanceof Calendar) { // this is needed ie. for MetaData dates
            return handleCalendar((Calendar) obj);
        } else if (obj instanceof User) {
            final User user = (User) obj;
            return new UserModel(user, this);
        } else if (obj instanceof Context) {
            // by default, Context would be considering like a map and we would not be able to use its specific methods.
            return new BeanModel(obj, this);
        } else {
            return super.wrap(obj);
        }
    }

    protected SimpleDate handleCalendar(Calendar cal) {
        return new SimpleDate(cal.getTime(), TemplateDateModel.DATETIME);
    }

}