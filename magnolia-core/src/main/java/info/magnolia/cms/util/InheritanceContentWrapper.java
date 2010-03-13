/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.cms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.AbstractContent;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;


/**
 * This wrapper inherits content from the parent hierarchy. The method {@link #isAnchor()} defines
 * the anchor to which the inheritance is performed relative to. By default the anchor is a page
 * (mgnl:content).
 * <p>
 * The inheritance is then performed as follows:
 * <ul>
 * <li>try to get the content directly</li>
 * <li>find next anchor</li>
 * <li>try to get the content from the anchor</li>
 * <li>repeat until no anchor can be found anymore (root)</li>
 * </ul>
 * <p>
 * The {@link #getChildren()} methods merge the direct and inherited children by first adding the
 * inherited children to the collection and then the direct children.
 * @author pbracher
 * @version $Id$
 */
public class InheritanceContentWrapper extends ContentWrapper {

    private static Logger log = LoggerFactory.getLogger(InheritanceContentWrapper.class);

    /**
     * True if this node were achieved through inheritance
     */
    private boolean inherited;

    public InheritanceContentWrapper(Content wrappedContent, boolean inherited) {
        super(wrappedContent);
        this.inherited = inherited;
    }

    /**
     * Starts inheritance for 
     * @param node
     */
    public InheritanceContentWrapper(Content node) {
        this(node, false);
    }
    
    @Override
    public boolean hasContent(String name) throws RepositoryException {
        return getContentSafely(name) != null;
    }
    
    @Override
    public Content getContent(String name) throws RepositoryException {
        Content inherited = getContentSafely(name);
        if(inherited == null){
            throw new PathNotFoundException("Can't inherit a node [" + name + "] on node [" + getWrappedContent().getHandle() + "]");
        }
        return inherited;
    }

    @Override
    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria){
        List children = new ArrayList();

        // add inherited children
        try {
            Content inherited = getContentSafely(findNextAnchor(), resolveInnerPath());
            if(inherited != null){
                children.addAll(((AbstractContent)inherited).getChildren(filter, namePattern, orderCriteria));
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't inherit children from " + getWrappedContent(), e);
        }
        
        // add direct children
        children.addAll(((AbstractContent)getWrappedContent()).getChildren(filter, namePattern, orderCriteria));
        if(orderCriteria != null){
            Collections.sort(children, orderCriteria);
        }
        
        return wrapContentNodes(children);
    }

    /**
     * Returns the inner path of the this node up to the anchor.
     */
    protected String resolveInnerPath() throws RepositoryException {
        final String path;
        InheritanceContentWrapper anchor = findAnchor();
        // if no anchor left we are relative to the root
        if(anchor == null){
            path = this.getHandle();
        }
        else{
            path = StringUtils.substringAfter(this.getHandle(), anchor.getHandle());
        }
        return StringUtils.removeStart(path,"/");
    }

    /**
     * This method returns null if no content has been found.
     */
    protected Content getContentSafely(String name) throws RepositoryException {
        if(getWrappedContent().hasContent(name)){
            return super.getContent(name);
        }
        
        String innerPath = resolveInnerPath() + "/" + name;
        innerPath = StringUtils.removeStart(innerPath,"/");
        
        Content inherited = getContentSafely(findNextAnchor(), innerPath);
        return inherited;
    }

    /**
     * This method returns null if no content has been found.
     */
    protected Content getContentSafely(InheritanceContentWrapper anchor, String path) throws RepositoryException{
        if(anchor == null){
            return null;
        }
        if(StringUtils.isEmpty(path)){
            return anchor;
        }
        return anchor.getContentSafely(path);        
    }

    /**
     * Find the anchor for this node.
     */
    protected InheritanceContentWrapper findAnchor() throws RepositoryException{
        if(getLevel() ==0){
            return null;
        }
        if(isAnchor()){
            return this;
        }
        // until the current node is the anchor
        return ((InheritanceContentWrapper)getParent()).findAnchor();
    }
    
    /**
     * Find next anchor.
     */
    protected InheritanceContentWrapper findNextAnchor() throws RepositoryException{
        final InheritanceContentWrapper currentAnchor = findAnchor();
        if(currentAnchor != null && getLevel() >0){
            return ((InheritanceContentWrapper)currentAnchor.getParent()).findAnchor();
        }
        return null;
    }

    /**
     * True if this node is an anchor. By default true if this node is of type mgnl:content (page)
     */
    protected boolean isAnchor() {
        return isNodeType(ItemType.CONTENT.getSystemName());
    }
    
    public NodeData getNodeData(String name) {
        try {
            if (getWrappedContent().hasNodeData(name)) {
                return getWrappedContent().getNodeData(name);
            }
            Content inherited = getContentSafely(findNextAnchor(), resolveInnerPath());
            if(inherited != null){
                return inherited.getNodeData(name);
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't inherit nodedata " + name + "  for " + getWrappedContent(), e);

        }
        // creates a none existing node data in the standard manner
        return super.getNodeData(name);
    }

    /**
     * Wrap returned nodes. Sets the inherited flag
     */
    protected Content wrap(Content node) {
        // only wrap once
        if(node instanceof InheritanceContentWrapper){
            return node;
        }
        // set the inherited flag
        boolean inherited = !isSubNode(node);
        return new InheritanceContentWrapper(node, inherited);
    }

    /**
     * True if the current node is an ancestor of node 
     */
    protected boolean isSubNode(Content node) {
        return node.getHandle().startsWith(getWrappedContent().getHandle());
    }

    public boolean isInherited() {
        return this.inherited;
    }
}
