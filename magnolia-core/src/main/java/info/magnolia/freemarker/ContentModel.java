/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNodeModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Chris Miner
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentModel implements TemplateHashModelEx, TemplateNodeModel, TemplateScalarModel {
    private final Content content;
    private final MagnoliaContentWrapper wrapper;

    ContentModel(Content content, MagnoliaContentWrapper wrapper) {
        this.content = content;
        this.wrapper = wrapper;
    }

    public String getAsString() throws TemplateModelException {
        return content.getName();
    }

    public TemplateModel get(String key) throws TemplateModelException {
        final Object result;

        if (key.equals("@handle")) {
            result = content.getHandle();
        } else if (key.equals("@uuid")) {
            result = content.getUUID();
        } else if (key.equals("@name")) {
            result = content.getName();
        } else if (key.equals("MetaData")) {
            result = content.getMetaData();
        } else {
            // try for node data or child node
            try {
                if (content.hasNodeData(key)) {
                    result = content.getNodeData(key);
                } else {
                    if(content.hasContent(key)){
                        result = content.getContent(key);
                    }
                    else{
                        result = null;
                    }
                }
            } catch (RepositoryException e) {
                throw new TemplateModelException(e);
            }
        }
        return wrapper.wrap(result);
    }

    public boolean isEmpty() throws TemplateModelException {
        return (size() == 0);
    }

    public int size() throws TemplateModelException {
        return content.getNodeDataCollection().size();
    }

    public TemplateCollectionModel keys() throws TemplateModelException {
        final Iterator it = IteratorUtils.transformedIterator(content.getNodeDataCollection().iterator(), new Transformer() {
            public Object transform(Object input) {
                return ((NodeData) input).getName();
            }
        });
        return new SimpleCollection(it);
    }

    public TemplateCollectionModel values() throws TemplateModelException {
        return (TemplateCollectionModel) wrapper.wrap(content.getNodeDataCollection().iterator());
    }

    public TemplateNodeModel getParentNode() throws TemplateModelException {
        try {
            // todo : check if this is the root?
            // content.getLevel() == 0;
            final Content parent = content.getParent();
            return (TemplateNodeModel) wrapper.wrap(parent);
        } catch (RepositoryException e) {
            throw new TemplateModelException("Can't get parent of " + content + ":" + e.getMessage(), e);
        }
    }

    /**
     * This returns all children, except nodes or jcr: types and mgnl:metaData
     */
    public TemplateSequenceModel getChildNodes() throws TemplateModelException {
        final Collection children = ContentUtil.getAllChildren(content);
        return (TemplateSequenceModel) wrapper.wrap(children);
    }

    public String getNodeName() throws TemplateModelException {
        return content.getName();
    }

    public String getNodeType() throws TemplateModelException {
        try {
            return content.getNodeTypeName();
        } catch (RepositoryException e) {
            throw new TemplateModelException("Can't get node type of " + content + ":" + e.getMessage(), e);
        }
    }

    public String getNodeNamespace() throws TemplateModelException {
        return null; // non XML implementation
    }

    public Content asContent() {
        return this.content;
    }
}
