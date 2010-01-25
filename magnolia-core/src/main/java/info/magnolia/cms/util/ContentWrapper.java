/**
 * This file Copyright (c) 2007-2009 Magnolia International
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
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
 * <li>{@link #getContent(String)}</li>
 * <li>{@link #getNodeData(String)}</li>
 * <li>{@link #getNodeDataCollection(String)}</li>
 * <li>{@link #wrap(Content)}</li>
 * <li>{@link #wrap(NodeData)}</li>
 * </ul>
 * 
 * @author pbaerfuss
 * @version $Id$
 *
 */
public abstract class ContentWrapper extends AbstractContent {

    private Content wrappedContent;

    public ContentWrapper() {
    }

    public ContentWrapper(Content wrappedContent) {
        this.wrappedContent = wrappedContent;
    }

    public Content getWrappedContent() {
        return this.wrappedContent;
    }

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
    
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append(" for ");
        buffer.append(super.toString());
        return buffer.toString();
    }

    // wrapping methods below - methods returning Content wrap it using #wrap(), methods returning Collection<Content> wrap it with #wrapContentNodes()
    public void addMixin(String type) throws RepositoryException {
        this.getWrappedContent().addMixin(type);
    }

    public Version addVersion() throws RepositoryException {
        return this.getWrappedContent().addVersion();
    }

    public Version addVersion(Rule rule) throws RepositoryException {
        return this.getWrappedContent().addVersion(rule);
    }

    public Content createContent(String name, String contentType) throws RepositoryException {
        return wrap(this.getWrappedContent().createContent(name, contentType));
    }

    public void delete() throws RepositoryException {
        this.getWrappedContent().delete();
    }
    
    public void deleteNodeData(String name) throws RepositoryException {
        this.getWrappedContent().deleteNodeData(name);
    }

    public VersionIterator getAllVersions() throws RepositoryException {
        return this.getWrappedContent().getAllVersions();
    }

    public Content getAncestor(int level) throws RepositoryException {
        return wrap(this.getWrappedContent().getAncestor(level));
    }

    public Collection<Content> getAncestors() throws RepositoryException {
        return wrapContentNodes(this.getWrappedContent().getAncestors());
    }

    public ContentVersion getBaseVersion() throws RepositoryException {
        return this.getWrappedContent().getBaseVersion();
    }

    public Content getChildByName(String namePattern) {
        return wrap(this.getWrappedContent().getChildByName(namePattern));
    }

    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria) {
        return wrapContentNodes(((AbstractContent) getWrappedContent()).getChildren(filter, namePattern, orderCriteria));
    }

    public Content getContent(String name) throws RepositoryException {
        return wrap(this.getWrappedContent().getContent(name));
    }

    public String getHandle() {
        return this.getWrappedContent().getHandle();
    }

    public int getIndex() throws RepositoryException {
        return this.getWrappedContent().getIndex();
    }

    public ItemType getItemType() throws RepositoryException {
        return this.getWrappedContent().getItemType();
    }

    public Node getJCRNode() {
        return this.getWrappedContent().getJCRNode();
    }

    public int getLevel() throws RepositoryException {
        return this.getWrappedContent().getLevel();
    }

    public Lock getLock() throws RepositoryException {
        return this.getWrappedContent().getLock();
    }

    public MetaData getMetaData() {
        return this.getWrappedContent().getMetaData();
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.getWrappedContent().getMixinNodeTypes();
    }

    public String getName() {
        return this.getWrappedContent().getName();
    }
    
    @Override
    public NodeData getNodeData(String name, int type) throws RepositoryException {
        return wrap(((AbstractContent)getWrappedContent()).getNodeData(name, type));
    }

    public Collection<NodeData> getNodeDataCollection(String namePattern) {
        return wrapNodeDatas(this.getWrappedContent().getNodeDataCollection(namePattern));
    }

    public NodeType getNodeType() throws RepositoryException {
        return this.getWrappedContent().getNodeType();
    }

    public String getNodeTypeName() throws RepositoryException {
        return this.getWrappedContent().getNodeTypeName();
    }

    public Content getParent() throws RepositoryException {
        return wrap(this.getWrappedContent().getParent());
    }

    public String getTemplate() {
        return this.getWrappedContent().getTemplate();
    }

    public String getTitle() {
        return this.getWrappedContent().getTitle();
    }

    public String getUUID() {
        return this.getWrappedContent().getUUID();
    }

    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        return this.getWrappedContent().getVersionedContent(versionName);
    }

    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        return this.getWrappedContent().getVersionedContent(version);
    }

    public VersionHistory getVersionHistory() throws RepositoryException {
        return this.getWrappedContent().getVersionHistory();
    }

    public Workspace getWorkspace() throws RepositoryException {
        return this.getWrappedContent().getWorkspace();
    }

    /**
     * Uses {@link #getContent(String)} and caches {@link PathNotFoundException} to make it easier to extend this class.
     */
    public boolean hasContent(String name) throws RepositoryException {
        try{
            getContent(name);
        }
        catch(PathNotFoundException e){
            return false;
        }
        return true;
    }

    public boolean hasMetaData() {
        return this.getWrappedContent().hasMetaData();
    }

    /**
     * Uses {@link #getNodeData(String)} and {@link NodeData#isExist()} to make it easier to extend this class.
     */
    public boolean hasNodeData(String name) throws RepositoryException {
        return getNodeData(name).isExist();
    }

    public boolean holdsLock() throws RepositoryException {
        return this.getWrappedContent().holdsLock();
    }

    public boolean isGranted(long permissions) {
        return this.getWrappedContent().isGranted(permissions);
    }

    public boolean isLocked() throws RepositoryException {
        return this.getWrappedContent().isLocked();
    }

    public boolean isModified() {
        return this.getWrappedContent().isModified();
    }

    public boolean isNodeData(String path) throws RepositoryException {
        return this.getWrappedContent().isNodeData(path);
    }

    public boolean isNodeType(String type) {
        return this.getWrappedContent().isNodeType(type);
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws RepositoryException {
        return this.getWrappedContent().lock(isDeep, isSessionScoped, yieldFor);
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws RepositoryException {
        return this.getWrappedContent().lock(isDeep, isSessionScoped);
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        this.getWrappedContent().orderBefore(srcName, beforeName);
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        this.getWrappedContent().refresh(keepChanges);
    }

    public void removeMixin(String type) throws RepositoryException {
        this.getWrappedContent().removeMixin(type);
    }

    public void removeVersionHistory() throws RepositoryException {
        this.getWrappedContent().removeVersionHistory();
    }

    public void restore(String versionName, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restore(versionName, removeExisting);
    }

    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restore(version, removeExisting);
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restore(version, relPath, removeExisting);
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws RepositoryException {
        this.getWrappedContent().restoreByLabel(versionLabel, removeExisting);
    }

    public void save() throws RepositoryException {
        this.getWrappedContent().save();
    }

    public void unlock() throws RepositoryException {
        this.getWrappedContent().unlock();
    }

    public void updateMetaData() throws RepositoryException {
        this.getWrappedContent().updateMetaData();
    }

    public HierarchyManager getHierarchyManager(){
        return this.getWrappedContent().getHierarchyManager();
    }

}