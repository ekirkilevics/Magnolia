/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collection;

/**
 * Implementing some default behavior.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public abstract class AbstractNodeData implements NodeData{
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractNodeData.class);

    protected String name;
    protected Content parent;
    private int multiValue = MULTIVALUE_UNDEFINED;

    protected AbstractNodeData(Content parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public HierarchyManager getHierarchyManager() {
        return parent.getHierarchyManager();
    }

    public String getName() {
        return this.name;
    }

    public String getHandle() {
        return Path.getAbsolutePath(parent.getHandle(), name);
    }

    public boolean isGranted(long permissions) {
        return getHierarchyManager().getAccessManager().isGranted(getHandle(), permissions);
    }

    public String getString(String lineBreak) {
        return getString().replaceAll("\n", lineBreak); //$NON-NLS-1$
    }

    public Content getParent() throws AccessDeniedException, ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return this.parent;
    }

    public void setParent(Content parent) {
        this.parent = parent;
    }

    public Content getReferencedContent(String repositoryId) throws PathNotFoundException, RepositoryException {
        if(this.getHierarchyManager().getName().equals(repositoryId)){
            return getReferencedContent();
        }
        return getReferencedContent(MgnlContext.getHierarchyManager(repositoryId));
    }

    public Content getReferencedContent() throws PathNotFoundException, RepositoryException {
        return getReferencedContent(this.getHierarchyManager());
    }

    protected Content getReferencedContent(HierarchyManager hm) throws PathNotFoundException, RepositoryException {
        if(!isExist()){
            return null;
        }
        // node containing this property
        Content node = getParent();
        Content refNode = null;

        int type = getType();

        if (type == PropertyType.REFERENCE) {
            refNode = getContentFromJCRReference();
        } else if (type == PropertyType.PATH || type == PropertyType.STRING) {
            final String pathOrUUID = this.getString();
            if (StringUtils.isNotBlank(pathOrUUID)) {
                // is this relative path?
                if (!pathOrUUID.startsWith("/") && node.hasContent(pathOrUUID)) {
                    refNode = node.getContent(pathOrUUID);
                } else if (pathOrUUID.startsWith("/") && hm.isExist(pathOrUUID)){
                    refNode = hm.getContent(pathOrUUID);
                }

                // we support uuids as strings
                if (refNode == null && type == PropertyType.STRING && !StringUtils.contains(pathOrUUID, "/")) {
                    try {
                        refNode = hm.getContentByUUID(pathOrUUID);
                    } catch (ItemNotFoundException e) {
                        // this is not an uuid
                    }
                }
            }
        }

        if(refNode==null){
            throw new ItemNotFoundException("can't find referenced node for value [" + getString() + "]");
        }

        return refNode;
    }

    /**
     * Specific implementation for retrieving the referenced node when using a property of type REFERENCE.
     */
    protected abstract Content getContentFromJCRReference() throws RepositoryException;

    public int isMultiValue() {
        if(multiValue == MULTIVALUE_UNDEFINED) {
            try {
                if (isExist()) {
                    getJCRProperty().getValue();
                    multiValue = MULTIVALUE_FALSE;
                }

            } catch (ValueFormatException e) {
                multiValue = MULTIVALUE_TRUE;

            } catch (Exception e) {
                log.debug(e.getMessage(), e);
            }
        }
        return this.multiValue;
    }

    public String getAttribute(String name) {
        throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
    }

    public Collection<String> getAttributeNames() throws RepositoryException {
        throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
    }

    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getHierarchyManager().getName()).append(":");
        buffer.append(getHandle());
        buffer.append("[");
        buffer.append(NodeDataUtil.getTypeName(this));
        buffer.append("]");

        return buffer.toString();
    }
}