/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
