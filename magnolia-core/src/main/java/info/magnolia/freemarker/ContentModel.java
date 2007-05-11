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
        } else if (key.equals("MetaData")) {
            result = content.getMetaData();
        } else {
            // try for node data or child node
            try {
                if (content.hasNodeData(key)) {
                    result = content.getNodeData(key);
                } else {
                    result = content.getChildByName(key);
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
     * This returns all children, no matter what their type is.
     */
    public TemplateSequenceModel getChildNodes() throws TemplateModelException {
        final Collection children = content.getChildren((String) null);
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
}