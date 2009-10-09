/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;


/**
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

    public InheritanceContentWrapper(Content node) {
        this(node, false);
    }

    public boolean hasContent(String name) {
        try {
            getContent(name);
        }
        catch (RepositoryException e) {
            return false;
        }
        return true;
    }

    public Content getContent(String name) throws RepositoryException {
        Content found = findContentByInheritance(name);
        if(found != null){
            boolean inherited = !found.getHandle().startsWith(getWrappedContent().getHandle());
            return new InheritanceContentWrapper(found, inherited);
        }

        throw new PathNotFoundException("Can't inherit a node [" + name + "] on node [" + getWrappedContent().getHandle() + "]");
    }


    public Collection<Content> getChildren() {
        return getChildren(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER);
    }

    public Collection<Content> getChildren(ContentFilter filter) {
        return getChildren(filter, null);
    }

    public Collection<Content> getChildren(ContentFilter filter, Comparator<Content> orderCriteria) {
        List children = new ArrayList();
        try {
            collectInheritedChildren(filter, children);
            for (Iterator iterator = getWrappedContent().getChildren(filter).iterator(); iterator.hasNext();) {
                Content child = (Content) iterator.next();
                children.add(wrap(child));
            }
        }
        catch (RepositoryException e) {
            log.error("Can't collect inherited nodes",e);
            return Collections.EMPTY_LIST;
        }
        if(orderCriteria != null){
            Collections.sort(children, orderCriteria);
        }
        return children;
    }

    protected void collectInheritedChildren(ContentFilter filter, List children) throws RepositoryException {
        Content found = findContentByInheritance(findNextAnchor(), resolveInnerPath());
        if(found != null){
            for (Iterator iterator = found.getChildren(filter).iterator(); iterator.hasNext();) {
                Content child = (Content) iterator.next();
                children.add(new InheritanceContentWrapper(child, true));
            }
            ((InheritanceContentWrapper)wrap(found)).collectInheritedChildren(filter, children);
        }
    }



    protected Content findContentByInheritance(String name) throws RepositoryException {
        InheritanceContentWrapper anchor = findAnchor();
        String path = resolveInnerPath() + "/" + name;
        path = StringUtils.removeStart(path,"/");

        Content found = findContentByInheritance(anchor, path);
        return found;
    }

    protected String resolveInnerPath() throws RepositoryException {
        String path = StringUtils.substringAfter(this.getHandle(), findAnchor().getHandle());
        path =  StringUtils.removeStart(path,"/");
        return path;
    }

    /**
     * This method returns null if no content has been found.
     */
    protected Content findContentByInheritance(InheritanceContentWrapper anchor, String path) throws RepositoryException{
        if(anchor ==null){
            return null;
        }
        if(StringUtils.isEmpty(path)){
            return anchor.getWrappedContent();
        }
        Content unwrapped = anchor.getWrappedContent();
        if(unwrapped.hasContent(path)){
            return unwrapped.getContent(path);
        }
        else{
            return findContentByInheritance(anchor.findNextAnchor(), path);
        }
    }

    protected InheritanceContentWrapper findNextAnchor() throws RepositoryException{
        final InheritanceContentWrapper currentAnchor = findAnchor();
        if(currentAnchor != null && getLevel() >0){
            return ((InheritanceContentWrapper)currentAnchor.getParent()).findAnchor();
        }
        return null;
    }

    protected InheritanceContentWrapper findAnchor() throws RepositoryException{
        if(getLevel() ==0){
            return null;
        }
        if(isNodeType(ItemType.CONTENT.getSystemName())){
            return this;
        }
        return ((InheritanceContentWrapper)getParent()).findAnchor();
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return getNodeData(name).isExist();
    }

    public NodeData getNodeData(String name) {
        try {
            if (getWrappedContent().hasNodeData(name)) {
                return getWrappedContent().getNodeData(name);
            }
            else {
                Content found = findContentByInheritance(findNextAnchor(), resolveInnerPath());
                if(found != null){
                    return wrap(found).getNodeData(name);
                }
            }
        }
        catch (RepositoryException e) {
            log.error("Can't inherit nodedata", e);
        }
        // creates a none existing node data in the standard manner
        return getWrappedContent().getNodeData(name);
    }

    protected Content wrap(Content node) {
        return new InheritanceContentWrapper(node);
    }

    public boolean isInherited() {
        return this.inherited;
    }
}