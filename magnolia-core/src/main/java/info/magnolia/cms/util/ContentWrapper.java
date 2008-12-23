/**
 * This file Copyright (c) 2007-2008 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;

import java.util.Collection;
import java.util.Comparator;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.ClassUtils;

public abstract class ContentWrapper implements Content {

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
     * Override if a wrapper wants to wrap returned content objects as well (by getContent(), getParent(), ...
     */
    protected Content wrap(Content node) {
        return node;
    }

    public void addMixin(String type) throws RepositoryException {
        this.getWrappedContent().addMixin(type);
    }

    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getWrappedContent().addVersion();
    }

    public Version addVersion(Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getWrappedContent().addVersion(rule);
    }

    public Content createContent(String name, ItemType contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createContent(name, contentType);
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createContent(name, contentType);
    }

    public Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createContent(name);
    }

    public Content createContentNode(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createContentNode(name);
    }

    public NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createNodeData(name, type);
    }

    public NodeData createNodeData(String name, Object obj) throws RepositoryException {
        return this.getWrappedContent().createNodeData(name, obj);
    }

    public NodeData createNodeData(String name, Value value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createNodeData(name, value);
    }

    public NodeData createNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createNodeData(name, value);
    }

    public NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().createNodeData(name);
    }

    public void delete() throws RepositoryException {
        this.getWrappedContent().delete();
    }

    public void delete(String path) throws RepositoryException {
        this.getWrappedContent().delete(path);
    }

    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        this.getWrappedContent().deleteNodeData(name);
    }

    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getWrappedContent().getAllVersions();
    }

    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().getAncestor(digree);
    }

    public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        return this.getWrappedContent().getAncestors();
    }

    public ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getWrappedContent().getBaseVersion();
    }

    public Content getChildByName(String namePattern) {
        return this.getWrappedContent().getChildByName(namePattern);
    }

    public Collection getChildren() {
        return this.getWrappedContent().getChildren();
    }

    public Collection getChildren(ContentFilter filter, Comparator orderCriteria) {
        return this.getWrappedContent().getChildren(filter, orderCriteria);
    }

    public Collection getChildren(ContentFilter filter) {
        return this.getWrappedContent().getChildren(filter);
    }

    public Collection getChildren(ItemType contentType, int sortCriteria) {
        return this.getWrappedContent().getChildren(contentType, sortCriteria);
    }

    public Collection getChildren(ItemType contentType) {
        return this.getWrappedContent().getChildren(contentType);
    }

    public Collection getChildren(String contentType, int sortCriteria) {
        return this.getWrappedContent().getChildren(contentType, sortCriteria);
    }

    public Collection getChildren(String contentType, String namePattern) {
        return this.getWrappedContent().getChildren(contentType, namePattern);
    }

    public Collection getChildren(String contentType) {
        return this.getWrappedContent().getChildren(contentType);
    }

    public Content getContent(String name, boolean create, ItemType contentType) throws AccessDeniedException, RepositoryException {
        return this.getWrappedContent().getContent(name, create, contentType);
    }

    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return wrap(this.getWrappedContent().getContent(name));
    }

    public Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().getContentNode(path);
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

    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.getWrappedContent().getLevel();
    }

    public Lock getLock() throws LockException, RepositoryException {
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

    public NodeData getNodeData(String name, boolean create) {
        return this.getWrappedContent().getNodeData(name, create);
    }

    public NodeData getNodeData(String name) {
        return this.getWrappedContent().getNodeData(name);
    }

    public Collection getNodeDataCollection() {
        return this.getWrappedContent().getNodeDataCollection();
    }

    public Collection getNodeDataCollection(String namePattern) {
        return this.getWrappedContent().getNodeDataCollection(namePattern);
    }

    public NodeType getNodeType() throws RepositoryException {
        return this.getWrappedContent().getNodeType();
    }

    public String getNodeTypeName() throws RepositoryException {
        return this.getWrappedContent().getNodeTypeName();
    }

    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
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

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getWrappedContent().getVersionHistory();
    }

    public Workspace getWorkspace() throws RepositoryException {
        return this.getWrappedContent().getWorkspace();
    }

    public boolean hasChildren() {
        return this.getWrappedContent().hasChildren();
    }

    public boolean hasChildren(String contentType) {
        return this.getWrappedContent().hasChildren(contentType);
    }

    public boolean hasContent(String name) throws RepositoryException {
        return this.getWrappedContent().hasContent(name);
    }

    public boolean hasMetaData() {
        return this.getWrappedContent().hasMetaData();
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return this.getWrappedContent().hasNodeData(name);
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

    public boolean isNodeData(String path) throws AccessDeniedException, RepositoryException {
        return this.getWrappedContent().isNodeData(path);
    }

    public boolean isNodeType(String type) {
        return this.getWrappedContent().isNodeType(type);
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException {
        return this.getWrappedContent().lock(isDeep, isSessionScoped, yieldFor);
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
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

    public void removeVersionHistory() throws AccessDeniedException, RepositoryException {
        this.getWrappedContent().removeVersionHistory();
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        this.getWrappedContent().restore(versionName, removeExisting);
    }

    public void restore(Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        this.getWrappedContent().restore(version, removeExisting);
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        this.getWrappedContent().restore(version, relPath, removeExisting);
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        this.getWrappedContent().restoreByLabel(versionLabel, removeExisting);
    }

    public void save() throws RepositoryException {
        this.getWrappedContent().save();
    }

    public NodeData setNodeData(String name, Value value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().setNodeData(name, value);
    }

    public NodeData setNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getWrappedContent().setNodeData(name, value);
    }

    public void unlock() throws LockException, RepositoryException {
        this.getWrappedContent().unlock();
    }

    public void updateMetaData() throws RepositoryException, AccessDeniedException {
        this.getWrappedContent().updateMetaData();
    }

    public HierarchyManager getHierarchyManager(){
        return this.getWrappedContent().getHierarchyManager();
    }

    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(ClassUtils.getShortClassName(getClass()));
        buffer.append(" for ");
        buffer.append(getWrappedContent().toString());
        return buffer.toString();
    }

    /**
     * @deprecated
     */
    public AccessManager getAccessManager() {
        return this.getWrappedContent().getAccessManager();
    }
}