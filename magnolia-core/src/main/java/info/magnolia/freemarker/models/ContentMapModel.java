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
package info.magnolia.freemarker.models;

import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNodeModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * A wrapper for Content nodes. Exposes properties using an HashModel (.property, ?size, ...) a hierarchy (TemplateNodeModel: ?children, ?parent, ...) and as a scalar (returns the node name)
 * 
 * @author Chris Miner
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentMapModel implements TemplateHashModelEx, TemplateNodeModel, TemplateScalarModel, AdapterTemplateModel {

    private static final Logger log = LoggerFactory.getLogger(ContentMapModel.class);

    static final MagnoliaModelFactory FACTORY = new MagnoliaModelFactory() {
        @Override
        public Class factoryFor() {
            return ContentMap.class;
        }

        @Override
        public AdapterTemplateModel create(Object object, ObjectWrapper wrapper) {
            return new ContentMapModel((ContentMap) object, (MagnoliaObjectWrapper) wrapper);
        }
    };

    private final ContentMap content;
    private final MagnoliaObjectWrapper wrapper;

    ContentMapModel(ContentMap content, MagnoliaObjectWrapper wrapper) {
        this.content = content;
        this.wrapper = wrapper;
    }

    @Override
    public String getAsString() {
        return (String) content.get("@name");
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        return wrapper.wrap(content.get(key));
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return (size() == 0);
    }

    @Override
    public int size() throws TemplateModelException {
        return content.size();
    }

    @Override
    public TemplateCollectionModel keys() throws TemplateModelException {
        return new SimpleCollection(content.keySet().iterator());
    }

    @Override
    public TemplateCollectionModel values() throws TemplateModelException {
        return (TemplateCollectionModel) wrapper.wrap(content.values().iterator());
    }

    @Override
    public TemplateNodeModel getParentNode() throws TemplateModelException {
        try {
            // todo : check if this is the root?
            // content.getLevel() == 0;
            final ContentMap parent = new ContentMap(content.getJCRNode().getParent());
            return (TemplateNodeModel) wrapper.wrap(parent);
        } catch (RepositoryException e) {
            throw new TemplateModelException("Can't get parent of " + content + ":" + e.getMessage(), e);
        }
    }

    /**
     * This returns all children, except nodes or jcr: types and mgnl:metaData.
     */
    @Override
    public TemplateSequenceModel getChildNodes() throws TemplateModelException {
        Collection<Node> children;
        try {
            children = NodeUtil.getChildren(content.getJCRNode());
        } catch (RepositoryException e) {
            log.error("Failed to read children of " + content.getJCRNode(), e);
            children = new ArrayList<Node>();
        }
        return (TemplateSequenceModel) wrapper.wrap(children);
    }

    @Override
    public String getNodeName() throws TemplateModelException {
        return (String) content.get("@name");
    }

    @Override
    public String getNodeType() throws TemplateModelException {
        return content.get("@nodeType").toString();
    }

    @Override
    public String getNodeNamespace() throws TemplateModelException {
        return null; // non XML implementation
    }

    @Override
    public Object getAdaptedObject(Class hint) {
        return this.content;
    }
}
