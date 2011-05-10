/**
 * This file Copyright (c) 2007-2011 Magnolia International
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

import info.magnolia.cms.core.AbstractContent;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

/**
 * A base class to implement content wrappers. All returned content objects, including collections, are also wrapped by calling the wrapping methods.
 * <p>
 * The following methods you might want to override:
 * <ul>
 * <li>{@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, String, java.util.Comparator)}</li>
 * <li>{@link #hasContent(String)}</li>
 * <li>{@link #getContent(String)}</li>
 * <li>{@link #getNodeData(String)}</li>
 * <li>{@link #getNodeDataCollection(String)}</li>
 * <li>{@link #wrap(Content)}</li>
 * <li>{@link #wrap(NodeData)}</li>
 * </ul>
 *
 * This default implementation assumes that the wrapped content is of type {@link AbstractContent}. If not you have to override the following methods:
 * <ul>
 * <li>{@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, String, java.util.Comparator)}</li>
 * <li>{@link #newNodeDataInstance(String, int, boolean)}</li>
 * </ul>
 *
 * @author pbaerfuss
 * @version $Id$
 *
 */
public abstract class ContentWrapper extends AbstractContent {

    private Content wrappedContent;

    /**
     * @deprecated since 4.3 - use {@link #ContentWrapper(info.magnolia.cms.core.Content)} instead.
     */
    public ContentWrapper() {
    }

    public ContentWrapper(Content wrappedContent) {
        this.wrappedContent = wrappedContent;
    }

    public Content getWrappedContent() {
        return this.wrappedContent;
    }

    /**
     * @deprecated since 4.3 - use {@link #ContentWrapper(info.magnolia.cms.core.Content)} instead.
     */
    public void setWrappedContent(Content wrappedContent) {
        this.wrappedContent = wrappedContent;
    }

    /**
     * Override if a wrapper wants to wrap returned content objects. This method is called by getContent(), getParent(), ...
     * The default implementation does nothing.
     */
    protected Content wrap(Content node) {
        return node;
    }

    /**
     * Override if a wrapper wants to wrap returned node data objects. The default implementation returns the original value.
     */
    protected NodeData wrap(NodeData nodeData) {
        return nodeData;
    }

    /**
     * Override if a wrapper wants to wrap returned collections as well (by getChildren(..), ...
     * Delegates to {@link #wrap(Content)}
     */
    protected Collection<Content> wrapContentNodes(Collection<Content> collection) {
        ArrayList<Content> wrapped = new ArrayList<Content>();
        for (Content content : collection) {
            wrapped.add(wrap(content));
        }
        return wrapped;
    }

    /**
     * Override if a wrapper wants to wrap returned collections as well (by getChildren(..), ...
     * Delegates to {@link #wrap(Content)}
     */
    protected Collection<NodeData> wrapNodeDatas(Collection<NodeData> collection) {
        ArrayList<NodeData> wrapped = new ArrayList<NodeData>();
        for (NodeData nodeData : collection) {
            wrapped.add(wrap(nodeData));
        }
        return wrapped;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append(" for ");
        buffer.append(super.toString());
        return buffer.toString();
    }

    @Override
    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria) {
        Content content = getWrappedContent();
        if(content instanceof AbstractContent){
            // first get the children from the wrapped content
            Collection<Content> children = ((AbstractContent) content).getChildren(ContentUtil.ALL_NODES_CONTENT_FILTER, namePattern, orderCriteria);
            // wrap the children
            Collection<Content> wrappedChildren = wrapContentNodes(children);
            // now we can apply the filter which might depend on the behavior of the wrapper
            Collection<Content> filteredChildren = new ArrayList<Content>();
            for (Content wrappedChild : wrappedChildren) {
                if(filter.accept(wrappedChild)){
                    filteredChildren.add(wrappedChild);
                }
            }
            return filteredChildren;
        }
        throw new IllegalStateException("This wrapper supports only wrapping AbstractContent objects by default. Please override this method.");
    }

    @Override
    public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
        Content content = getWrappedContent();
        if(content instanceof AbstractContent){
            return wrap(((AbstractContent)content).newNodeDataInstance(name, type, createIfNotExisting));
        }
        throw new IllegalStateException("This wrapper supports only wrapping AbstractContent objects by default. Please override this method.");
    }

    // --- only wrapping methods below
    // --- - methods returning Content should wrap it using #wrap()
    // --- - methods returning Collection<Content> should wrap it with #wrapContentNodes()

    @Override
    public void addMixin(String type) throws RepositoryException {
        this.getWrappedContent().addMixin(type);
    }

    @Override
    public Version addVersion() throws RepositoryException {
        return this.getWrappedContent().addVersion();
    }

    @Override
    public Version addVersion(Rule rule) throws RepositoryException {
        return this.getWrappedContent().addVersion(rule);
    }

    @Override
    public Content createContent(String name, String contentType) throws RepositoryException {
        return wrap(this.getWrappedContent().createContent(name, contentType));
    }

    @Override
    public void delete() throws RepositoryException {
        this.getWrappedContent().delete();
    }

    @Override
    public void deleteNodeData(String name) throws RepositoryException {
        this.getWrappedContent().deleteNodeData(name);
    }

    @Override
    public VersionIterator getAllVersions() throws RepositoryException {
        return this.getWrappedContent().getAllVersions();
    }

    @Override
    public Content getAncestor(int level) throws RepositoryException {
        return wrap(this.getWrappedContent().getAncestor(level));
    }

    @Override
    public Collection<Content> getAncestors() throws RepositoryException {
        return wrapContentNodes(this.getWrappedContent().getAncestors());
    }

    @Override
    public ContentVersion getBaseVersion() throws RepositoryException {
        return this.getWrappedContent().getBaseVersion();
    }

    /**
     * @deprecated since 4.3, either use {@link #getContent(String)} or {@link #getChildren(String)}
     */
    @Override
    public Content getChildByName(String namePattern) {
        return wrap(this.getWrappedContent().getChildByName(namePattern));
    }

    @Override
    public Content getContent(String name) throws RepositoryException {
        return wrap(this.getWrappedContent().getContent(name));
    }

    @Override
    public String getHandle() {
        return this.getWrappedContent().getHandle();
    }

    @Override
    public int getIndex() throws RepositoryException {
        return this.getWrappedContent().getIndex();
    }

    @Override
    public ItemType getItemType() throws RepositoryException {
        return this.getWrappedContent().getItemType();
    }

    @Override
    public Node getJCRNode() {
        return this.getWrappedContent().getJCRNode();
    }

    @Override
    public int getLevel() throws RepositoryException {
        return this.getWrappedContent().getLevel();
    }

    @Override
    public Lock getLock() throws RepositoryException {
        return this.getWrappedContent().getLock();
    }

    @Override
    public MetaData getMetaData() {
        return this.getWrappedContent().getMetaData();
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.getWrappedContent().getMixinNodeTypes();
    }

    @Override
    public String getName() {
        return this.getWrappedContent().getName();
    }

    @Override
    public Collection<NodeData> getNodeDataCollection(String namePattern) {
        return wrapNodeDatas(this.getWrappedContent().getNodeDataCollection(namePattern));
    }

    @Override
    public NodeType getNodeType() throws RepositoryException {
        return this.getWrappedContent().getNodeType();
    }

    @Override
    public String getNodeTypeName() throws RepositoryException {
        return this.getWrappedContent().getNodeTypeName();
    }

    @Override
    public Content getParent() throws RepositoryException {
        return wrap(this.getWrappedContent().getParent());
    }

    @Override
    public String getTemplate() {
        return this.getWrappedContent().getTemplate();
    }

    @Override
    public String getTitle() {
        return this.getWrappedContent().getTitle();
    }

    @Override
    public String getUUID() {
        return this.getWrappedContent().getUUID();
    }

    @Override
    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        return this.getWrappedContent().getVersionedContent(versionName);
    }

    @Override
    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        return this.getWrappedContent().getVersionedContent(version);
    }

    @Override
    public VersionHistory getVersionHistory() throws RepositoryException {
        return this.getWrappedContent().getVersionHistory();
    }

    @Override
    public Workspace getWorkspace() throws RepositoryException {
        return this.getWrappedContent().getWorkspace();
    }

    @Override
    public boolean hasContent(String name) throws RepositoryException {
        return getWrappedContent().hasContent(name);
    }

    @Override
    public boolean hasMetaData() {
        return this.getWrappedContent().hasMetaData();
    }

    @Override
    public boolean holdsLock() throws RepositoryException {
        return this.getWrappedContent().holdsLock();
    }

    @Override
    public boolean isGranted(long permissions) {
        return this.getWrappedContent().isGranted(permissions);
    }

    @Override
    public boolean isLocked() throws RepositoryException {
        return this.getWrappedContent().isLocked();
    }

    @Override
    public boolean isModified() {
        return this.getWrappedContent().isModified();
    }

    @Override
    public boolean isNodeData(String path) throws RepositoryException {
        return this.getWrappedContent().isNodeData(path);
    }

    @Override
    public boolean isNodeType(String type) {
        return this.getWrappedContent().isNodeType(type);
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws RepositoryException {
        return this.getWrappedContent().lock(isDeep, isSessionScoped, yieldFor);
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws RepositoryException {
        return this.getWrappedContent().lock(isDeep, isSessionScoped);
    }

    @Override
    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        this.getWrappedContent().orderBefore(srcName, beforeName);
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.getWrappedContent().refresh(keepChanges);
    }

    @Override
    public void removeMixin(String type) throws RepositoryException {
        this.getWrappedContent().removeMixin(type);
    }

    @Override
    public void removeVersionHistory() throws RepositoryException {
        this.getWrappedContent().removeVersionHistory();
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restore(versionName, removeExisting);
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restore(version, removeExisting);
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restore(version, relPath, removeExisting);
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restoreByLabel(versionLabel, removeExisting);
    }

    @Override
    public void save() throws RepositoryException {
        this.getWrappedContent().save();
    }

    @Override
    public void unlock() throws RepositoryException {
        this.getWrappedContent().unlock();
    }

    @Override
    public void updateMetaData() throws RepositoryException {
        this.getWrappedContent().updateMetaData();
    }

    @Override
    public HierarchyManager getHierarchyManager(){
        return this.getWrappedContent().getHierarchyManager();
    }

    @Override
    public boolean hasMixin(String mixinName) throws RepositoryException {
        return this.getWrappedContent().hasMixin(mixinName);
    }
}
