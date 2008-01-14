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
package info.magnolia.cms.core;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * $Id$
 */
public class MetaData {

    /**
     * Top level atoms viewed as metadata of the specified content these must be set by the authoring system itself, but
     * could be changed via custom templates if neccessary.
     */
    public static final String TITLE = "title"; //$NON-NLS-1$

    public static final String CREATION_DATE = "creationdate"; //$NON-NLS-1$

    public static final String LAST_MODIFIED = "lastmodified"; //$NON-NLS-1$

    public static final String LAST_ACTION = "lastaction"; //$NON-NLS-1$

    public static final String AUTHOR_ID = "authorid"; //$NON-NLS-1$

    public static final String ACTIVATOR_ID = "activatorid"; //$NON-NLS-1$

    public static final String TEMPLATE = "template"; //$NON-NLS-1$

    public static final String TEMPLATE_TYPE = "templatetype"; //$NON-NLS-1$

    public static final String ACTIVATED = "activated"; //$NON-NLS-1$

    /**
     * @deprecated we use jcr ordering instead
     */
    public static final String SEQUENCE_POS = "sequenceposition"; //$NON-NLS-1$

    public static final String DEFAULT_META_NODE = "MetaData"; //$NON-NLS-1$

    public static final int ACTIVATION_STATUS_NOT_ACTIVATED = 0;

    public static final int ACTIVATION_STATUS_MODIFIED = 1;

    public static final int ACTIVATION_STATUS_ACTIVATED = 2;

    /**
     * @deprecated unused
     */
    public static final long SEQUENCE_POS_COEFFICIENT = 1000;

    private static final Logger log = LoggerFactory.getLogger(MetaData.class);

    /**
     * meta data node
     */
    private Node node;

    private AccessManager accessManager;

    /**
     * Package private constructor
     * @param workingNode current <code>Node</code> on which <code>MetaData</code> is requested
     */
    MetaData(Node workingNode, AccessManager manager) {
        this.setMetaNode(workingNode, DEFAULT_META_NODE);
        this.setAccessManager(manager);
    }

    protected MetaData() {
    }

    public String getHandle() throws RepositoryException {
        return this.node.getPath();
    }

    public void setAccessManager(AccessManager manager) {
        this.accessManager = manager;
    }

    private void allowUpdate() throws AccessDeniedException {
        // if node is null, MetaData has not been created and allowUpdate can abort silently
        if (node == null) {
            return;
        }
        try {
            Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.WRITE);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new AccessDeniedException(re.getMessage());
        }
    }

    /**
     * MetaData should be created by the repository implementation based on the node type definition
     * @param workingNode
     * @param name
     */
    private void setMetaNode(Node workingNode, String name) {
        try {
            this.node = workingNode.getNode(name);
        }
        catch (PathNotFoundException e) {
            if (log.isDebugEnabled()) {
                try {
                    log.debug(workingNode.getPath() + " does not support MetaData");
                    log.debug("check node type definition of " + workingNode.getPrimaryNodeType().getName());
                }
                catch (RepositoryException re) {
                    // should never come here
                }
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

    /**
     * Get all meta data properties
     * @return property iterator
     */
    public PropertyIterator getProperties() {
        if (node == null) {
            return null;
        }
        try {
            return this.node.getProperties();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return null;
    }

    /**
     * Part of metadata, same as name of actual storage node. This value is unique at the hierarchy level context.
     * @return String value of the requested metadata
     */
    public String getLabel() {
        try {
            return this.node.getName();
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
            }
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * get property name with the prefix
     * @param name
     * @return name with namespace prefix
     */
    private String getInternalPropertyName(String name) {
        if (StringUtils.indexOf(name, ContentRepository.NAMESPACE_PREFIX + ":") != 0) {
            return ContentRepository.NAMESPACE_PREFIX + ":" + name;
        }
        return name;
    }

    /**
     * Part of metadata , could be used as html header.
     * @return String value of the requested metadata
     */
    public String getTitle() {
        return getStringProperty(this.getInternalPropertyName(TITLE));
    }

    /**
     * Part of metadata, could be used as html header.
     * @param value
     */
    public void setTitle(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(TITLE), value);
    }

    /**
     * Part of metadata, adds creation date of the current node
     */
    public void setCreationDate() throws AccessDeniedException {
        allowUpdate();
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(this.getInternalPropertyName(CREATION_DATE), value);
    }

    /**
     * Part of metadata, get creation date of the current node.
     * @return Calendar
     */
    public Calendar getCreationDate() {
        return this.getDateProperty(this.getInternalPropertyName(CREATION_DATE));
    }

    /**
     * Part of metadata, adds activated status of the current node
     */
    public void setActivated() throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(ACTIVATED), true);
    }

    /**
     * Part of metadata, adds activated status of the current node.
     */
    public void setUnActivated() throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(ACTIVATED), false);
    }

    /**
     * Part of metadata, get last activated status of the current node
     * @return Calendar
     */
    public boolean getIsActivated() {
        return getBooleanProperty(this.getInternalPropertyName(ACTIVATED));
    }

    /**
     * Returns one of the ACTIVATION_STATUS_* constants
     */
    public int getActivationStatus(){
        if (getIsActivated()) {
            if (getModificationDate() != null && getModificationDate().after(getLastActionDate())) {
                // node has been modified after last activation
                return ACTIVATION_STATUS_MODIFIED;
            }
            else {
                // activated and not modified ever since
                return ACTIVATION_STATUS_ACTIVATED;
            }
        }
        else {
            // never activated or deactivated
            return ACTIVATION_STATUS_NOT_ACTIVATED;
        }
    }

    /**
     * Part of metadata, adds activated date of the current node
     */
    public void setLastActivationActionDate() throws AccessDeniedException {
        allowUpdate();
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(this.getInternalPropertyName(LAST_ACTION), value);
    }

    /**
     * Part of metadata, get last activated/de- date of the current node
     * @return Calendar
     */
    public Calendar getLastActionDate() {
        return getDateProperty(this.getInternalPropertyName(LAST_ACTION));
    }

    /**
     * Part of metadata, adds modification date of the current node
     */
    public void setModificationDate() throws AccessDeniedException {
        allowUpdate();
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(this.getInternalPropertyName(LAST_MODIFIED), value);
    }

    /**
     * Part of metadata, get last modified date of the current node
     * @return Calendar
     */
    public Calendar getModificationDate() {
        return getDateProperty(this.getInternalPropertyName(LAST_MODIFIED));
    }

    /**
     * Part of metadata, last known author of this node.
     * @return String value of the requested metadata
     */
    public String getAuthorId() {
        return getStringProperty(this.getInternalPropertyName(AUTHOR_ID));
    }

    /**
     * Part of metadata, current logged-in author who did some action on this page.
     * @param value
     */
    public void setAuthorId(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(AUTHOR_ID), value);
    }

    /**
     * Part of metadata, last known activator of this node.
     * @return String value of the requested metadata
     */
    public String getActivatorId() {
        return getStringProperty(this.getInternalPropertyName(ACTIVATOR_ID));
    }

    /**
     * Part of metadata, current logged-in author who last activated this page
     * @param value
     */
    public void setActivatorId(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(ACTIVATOR_ID), value);
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     * @return String value of the requested metadata
     */
    public String getTemplate() {
        return getStringProperty(this.getInternalPropertyName(TEMPLATE));
    }

    /**
     * Part of metadata, template which will be used to render content of this node
     * @param value
     */
    public void setTemplate(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(TEMPLATE), value);
    }

    /**
     * Part of metadata, template type : JSP - Servlet - _xxx_
     * @param value
     */
    public void setTemplateType(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(TEMPLATE_TYPE), value);
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, String value) throws AccessDeniedException {
        allowUpdate();
        name = this.getInternalPropertyName(name);
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        catch (RepositoryException re) {
            throw new AccessDeniedException(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot set property - " + name); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, long value) throws AccessDeniedException {
        allowUpdate();
        name = this.getInternalPropertyName(name);
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot set property - " + name); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, double value) throws AccessDeniedException {
        allowUpdate();
        name = this.getInternalPropertyName(name);
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot set property - " + name); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, boolean value) throws AccessDeniedException {
        allowUpdate();
        name = this.getInternalPropertyName(name);
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot set property - " + name); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, Calendar value) throws AccessDeniedException {
        allowUpdate();
        name = this.getInternalPropertyName(name);
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot set property - " + name); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param name
     */
    public Calendar getDateProperty(String name) {
        name = this.getInternalPropertyName(name);
        try {
            return this.node.getProperty(name).getDate();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + name + "] in node " + this.node); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot get property - " + name); //$NON-NLS-1$
            }
        }
        return null;
    }

    /**
     * @param name
     */
    public boolean getBooleanProperty(String name) {
        name = this.getInternalPropertyName(name);
        try {
            return this.node.getProperty(name).getBoolean();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + name + "] in node " + this.node); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot get property - " + name); //$NON-NLS-1$
            }
        }
        return false;
    }

    /**
     * @param name
     */
    public double getDoubleProperty(String name) {
        name = this.getInternalPropertyName(name);
        try {
            return this.node.getProperty(name).getDouble();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + name + "] in node " + this.node); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot get property - " + name); //$NON-NLS-1$
            }
        }
        return 0d;
    }

    /**
     * @param name
     */
    public long getLongProperty(String name) {
        name = this.getInternalPropertyName(name);
        try {
            return this.node.getProperty(name).getLong();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + name + "] in node " + this.node); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot get property - " + name); //$NON-NLS-1$
            }
        }
        return 0L;
    }

    /**
     * Returns a String property. If the property does not exist, this will return an empty String.
     * @param name
     * @return the property value, never null
     */
    public String getStringProperty(String name) {
        name = this.getInternalPropertyName(name);
        try {
            return this.node.getProperty(name).getString();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + name + "] in node " + this.node); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot get property - " + name); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * remove specified property
     * @param name of the property to be removed
     * @throws PathNotFoundException if property does not exist
     * @throws RepositoryException if unable to remove
     */
    public void removeProperty(String name) throws PathNotFoundException, RepositoryException {
        this.node.getProperty(this.getInternalPropertyName(name)).remove();
    }

    /**
     * check if property exist
     * @param name
     * @return true if the specified property exist
     */
    public boolean hasProperty(String name) {
        try {
            return this.node.hasProperty(this.getInternalPropertyName(name));
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this).append("title", this.getTitle()) //$NON-NLS-1$
            .append("template", this.getTemplate()) //$NON-NLS-1$
            .append("authorId", this.getAuthorId()) //$NON-NLS-1$
            .append("label", this.getLabel()) //$NON-NLS-1$
            .append("activatorId", this.getActivatorId()) //$NON-NLS-1$
            .append("isActivated", this.getIsActivated()) //$NON-NLS-1$
            .append("creationDate", this.getCreationDate()) //$NON-NLS-1$
            .append("lastActionDate", this.getLastActionDate()) //$NON-NLS-1$
            .append("modificationDate", this.getModificationDate()) //$NON-NLS-1$
            .toString();
    }

}
