/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.freemarker.models;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModel;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkTransformer;
import info.magnolia.link.LinkTransformerManager;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

/**
 * A ModelFactory that instanciates an appropriate Freemarker model depending on the type of the property.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class NodeDataModelFactory implements MagnoliaModelFactory {
    static final NodeDataModelFactory INSTANCE = new NodeDataModelFactory();

    public Class factoryFor() {
        return NodeData.class;
    }

    public TemplateModel create(final Object object, final ObjectWrapper wrapper) {
        final MagnoliaObjectWrapper magnoliaWrapper = (MagnoliaObjectWrapper) wrapper;
        final NodeData nodeData = (NodeData) object;
        switch (nodeData.getType()) {
            case PropertyType.BOOLEAN:
                return nodeData.getBoolean() ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;

            case PropertyType.DATE:
                return ((MagnoliaObjectWrapper)wrapper).handleCalendar(nodeData.getDate());

            case PropertyType.DOUBLE:
                return new SimpleNumber(nodeData.getDouble());

            case PropertyType.LONG:
                return new SimpleNumber(nodeData.getLong());

            case PropertyType.STRING:
                final String s = nodeData.getString();
                final LinkTransformer t;
                // TODO : maybe this could be moved to LinkUtil
                if (MgnlContext.isWebContext()) {
                    final Content page = MgnlContext.getAggregationState().getMainContent();
                    if (page != null) {
                        t = LinkTransformerManager.getInstance().getRelative(page, true, true);
                    } else {
                        t = LinkTransformerManager.getInstance().getAbsolute(true, true, true);
                    }
                } else {
                    t = LinkTransformerManager.getInstance().getCompleteUrl(true, true);
                }
                final String transformedString = LinkUtil.convertUUIDsToLinks(s, t);
                return new SimpleScalar(transformedString);

            case PropertyType.BINARY:
                return new BinaryNodeDataModel(nodeData, magnoliaWrapper);

            case PropertyType.REFERENCE:
                try {
                    Content c = nodeData.getReferencedContent();
                    return new ContentModel(c, magnoliaWrapper);
                } catch (RepositoryException e) {
                    // TODO -- can't throw a TemplateModelException from here.
                    throw new RuntimeException("Can't retrieve referenced content from " + nodeData, e);
                }

//                case PropertyType.PATH:
//                case PropertyType.NAME:
            default:
                // TODO -- can't throw a TemplateModelException from here.
                throw new IllegalStateException("Unsupported property type: " + PropertyType.nameFromValue(nodeData.getType()));
        }

    }

}
