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
package info.magnolia.test.mock;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.NonExistingNodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;


/**
 * @version $Id$
 */
public class MockContent extends DefaultContent {

    public MockContent(String name) {
        this(new MockNode(name));
    }

    public MockContent(String name, ItemType type) {
        this(new MockNode(name, type.getSystemName()));
    }

    public MockContent(MockNode node) {
        super(node);
    }

    public MockContent(MockNode rootNode, String path) throws PathNotFoundException, RepositoryException, AccessDeniedException{
        super(rootNode, path);
    }

    public MockContent(MockNode rootNode, String path, String contentType) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        super(rootNode, path, contentType);
    }

    public void setUUID(String identifier) {
        ((MockNode) getJCRNode()).setIdentifier(identifier);
    }

    @Override
    public String getHandle() {
        Content parent;
        try {
            parent = getParent();
        }
        catch (ItemNotFoundException e) {
            // ok - then we don't have a parent...
            parent = null;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        String handle;
        if (parent != null && !parent.getName().equals("jcr:root")) {
            handle = parent.getHandle() + "/" + this.getName();
        } else {
            handle = "/" + this.getName();
        }
        return handle;
    }

    @Override
    public MockMetaData getMetaData() {
        try {
            if(!hasContent(MetaData.DEFAULT_META_NODE)){
                createContent(MetaData.DEFAULT_META_NODE, ItemType.NT_METADATA);
            }

            return new MockMetaData((MockContent) getContent(MetaData.DEFAULT_META_NODE));
        } catch (RepositoryException e) {
            throw new RuntimeException("Can't create/read the meta data node.", e);
        }
    }


    @Override
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Node parentNode = getJCRNode().getParent();
        return parentNode == null ? null: new MockContent((MockNode) parentNode);
    }

    @Override
    public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
        if(hasNodeData(name)){
            // TODO dlipp - isn't that the wrong - should MockNodeData just wrap Property?
            Property property = getJCRNode().getProperty(name);
            MockNodeData nd = new MockNodeData(property.getName(), property.getValue());
            nd.setParent(this);
            return nd;
        }
        else if(!createIfNotExisting){
            //&& type != PropertyType.BINARY){
            // binaries might have been created via property format or import, so we currently only have them as MockContent instances in the system
            // todo - better fix and/or remove them from child nodes ?
            return new NonExistingNodeData(getParent(), name);
        }
        else{
            MockNodeData nodeData;
            // TODO if(type == PropertyType.UNDEFINED){
            //    if (hasContent(name) && getContent(name).isNodeType(ItemType.NT_RESOURCE)) {
            //        type = PropertyType.BINARY;
            //    } - else ?

            if(type == PropertyType.BINARY){
                Content binaryNode = createContent(name, ItemType.NT_RESOURCE);
                nodeData = new BinaryMockNodeData(this, name, (MockContent) binaryNode);
            }
            else{
                nodeData = new MockNodeData(this, name, type);
            }
            addNodeData(nodeData);
            return nodeData;
        }
    }

    public void addNodeData(String name, Object value) {
        new MockNodeData(this, name, value);
    }

    public void addBinaryNodeData(String name, MockContent wrappedContent) {
        new BinaryMockNodeData(this, name, wrappedContent);
    }

    public void addNodeData(MockNodeData nd) {
        // TODO dlipp - how to treat BinaryNodeDatas?
        try {
            nd.setParent(this);
            getJCRNode().setProperty(nd.getName(), nd.getValue());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void addContent(MockContent content) {
        ((MockNode) getJCRNode()).addNode((MockNode) content.getJCRNode());
    }

    public void setName(String name) {
        ((MockNode)getJCRNode()).setName(name);
    }

    public MockMetaData createMetaData() {
        addContent(new MockContent("MetaData"));
        return getMetaData();
    }

    @Override
    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        MockContent c = new MockContent(name, new ItemType(contentType));
        addContent(c);

        if (c.isNodeType(ItemType.NT_RESOURCE)) {
            final BinaryMockNodeData binND = new BinaryMockNodeData(this, name, c);
            addNodeData(binND);
        }
        return c;
    }

    @Override
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new MockContent((MockNode) node, name));
    }

    @Override
    public void delete() throws RepositoryException {
        final MockNode parent = (MockNode) getParent().getJCRNode();
        ((MockSession) getJCRNode().getSession()).removeFromCache((MockNode) getJCRNode());
        final boolean removedFromParent = parent.removeFromChildren(getJCRNode());

        if (!removedFromParent) {
            throw new RepositoryException("MockContent could not delete itself");
        }
    }

    @Override
    protected Content wrapAsContent(Node node) {
        return new MockContent((MockNode)node);
    }

    @Override
    protected Content wrapAsContent(Node node, String name) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        return new MockContent((MockNode) node, name);
    }

    @Override
    protected Content wrapAsContent(Node node, String name, String contentType) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        return new MockContent((MockNode) node, name, contentType);
    }
}
