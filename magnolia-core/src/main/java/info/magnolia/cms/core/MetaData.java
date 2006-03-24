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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 *
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

    public static final String START_TIME = "starttime"; //$NON-NLS-1$

    public static final String END_TIME = "endtime"; //$NON-NLS-1$

    public static final String TEMPLATE = "template"; //$NON-NLS-1$

    public static final String TEMPLATE_TYPE = "templatetype"; //$NON-NLS-1$

    public static final String ACTIVATED = "activated"; //$NON-NLS-1$

    public static final String SEQUENCE_POS = "sequenceposition"; //$NON-NLS-1$

    /**
     * @deprecated all meta data properties should be under one single MetaData node
     */
    public static final String ACTIVATION_INFO = ".activationInfo"; //$NON-NLS-1$

    public static final String DEFAULT_META_NODE = "MetaData"; //$NON-NLS-1$

    public static final long SEQUENCE_POS_COEFFICIENT = 1000;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MetaData.class);

    /**
     * meta data node
     */
    private Node node;

    private AccessManager accessManager;

    /**
     * Package private constructor
     *
     * @param workingNode current <code>Node</code> on which <code>MetaData</code> is requested
     */
    MetaData(Node workingNode, AccessManager manager) {
        this(workingNode, DEFAULT_META_NODE, manager);
    }

    /**
     * constructor
     *
     * @param workingNode current <code>Node</code> on which <code>MetaData</code> is requested
     * @param nodeName    under which this data is saved
     */
    MetaData(Node workingNode, String nodeName, AccessManager manager) {
        this.setMetaNode(workingNode, DEFAULT_META_NODE);
        this.setAccessManager(manager);
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
     *
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
     *
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
     *
     * @return String value of the requested metadata
     */
    public String getLabel() {
        try {
            return this.node.getName();
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled())
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * get property name with the prefix
     *
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
     *
     * @return String value of the requested metadata
     */
    public String getTitle() {
        return getStringProperty(this.getInternalPropertyName(TITLE));
    }

    /**
     * Part of metadata, could be used as html header.
     *
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
     *
     * @return Calendar
     */
    public Calendar getCreationDate() {
        return this.getDateProperty(this.getInternalPropertyName(CREATION_DATE));
    }

    /**
     * Part of metadata, adds sequence number of the current node
     *
     * @deprecated use JCR ordering
     */
    public void setSequencePosition(long seqPos) throws AccessDeniedException {
        if (this.node == null) {
            return;
        }

        allowUpdate();
        long newPos = (seqPos == 0) ? new Date().getTime() * SEQUENCE_POS_COEFFICIENT : seqPos;

        try {
            this.node.getProperty(this.getInternalPropertyName(SEQUENCE_POS)).setValue(newPos);
        }
        catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(this.getInternalPropertyName(SEQUENCE_POS), newPos);
            }
            catch (RepositoryException e) {
                // ignore?
            }
        }
        catch (RepositoryException re) {
            // ignore?
        }
    }

    /**
     * Part of metadata, adds sequence number of the current node
     *
     * @deprecated use JCR ordering
     */
    public void setSequencePosition() throws AccessDeniedException {
        setSequencePosition(0);
    }

    /**
     * Part of metadata, get sequence position of the current node
     *
     * @return long
     * @deprecated use JCR ordering
     */
    public long getSequencePosition() {
        try {
            return this.node.getProperty(this.getInternalPropertyName(SEQUENCE_POS)).getLong();
        }
        catch (PathNotFoundException ee) {
            Calendar cd = getCreationDate();
            if (cd != null) {
                return cd.getTimeInMillis() * SEQUENCE_POS_COEFFICIENT;
            }
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData"); //$NON-NLS-1$
                log.debug("cannot get property - " + SEQUENCE_POS); //$NON-NLS-1$
            }
        }
        return 0;
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
     *
     * @return Calendar
     */
    public boolean getIsActivated() {
        return getBooleanProperty(this.getInternalPropertyName(ACTIVATED));
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
     *
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
     *
     * @return Calendar
     */
    public Calendar getModificationDate() {
        return getDateProperty(this.getInternalPropertyName(LAST_MODIFIED));
    }

    /**
     * Part of metadata, last known author of this node.
     *
     * @return String value of the requested metadata
     */
    public String getAuthorId() {
        return getStringProperty(this.getInternalPropertyName(AUTHOR_ID));
    }

    /**
     * Part of metadata, current logged-in author who did some action on this page.
     *
     * @param value
     */
    public void setAuthorId(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(AUTHOR_ID), value);
    }

    /**
     * Part of metadata, last known activator of this node.
     *
     * @return String value of the requested metadata
     */
    public String getActivatorId() {
        return getStringProperty(this.getInternalPropertyName(ACTIVATOR_ID));
    }

    /**
     * Part of metadata, current logged-in author who last activated this page
     *
     * @param value
     */
    public void setActivatorId(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(ACTIVATOR_ID), value);
    }

    /**
     * Part of metadata, node activation time
     */
    public void setStartTime(Calendar value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(START_TIME), value);
    }

    /**
     * Part of metadata, node activation time
     *
     * @return Calendar
     */
    public Calendar getStartTime() {
        return getDateProperty(this.getInternalPropertyName(START_TIME));
    }

    /**
     * Part of metadata, node de-activation time
     */
    public void setEndTime(Calendar value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(END_TIME), value);
    }

    /**
     * Part of metadata, node de-activation time
     *
     * @return Calendar
     */
    public Calendar getEndTime() {
        return getDateProperty(this.getInternalPropertyName(END_TIME));
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     *
     * @return String value of the requested metadata
     */
    public String getTemplate() {
        return getStringProperty(this.getInternalPropertyName(TEMPLATE));
    }

    /**
     * Part of metadata, template which will be used to render content of this node
     *
     * @param value
     */
    public void setTemplate(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(this.getInternalPropertyName(TEMPLATE), value);
    }

    /**
     * Part of metadata, template type : JSP - Servlet - _xxx_
     *
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
     *
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this).append("title", this.getTitle()) //$NON-NLS-1$
                .append("template", this.getTemplate()) //$NON-NLS-1$
                .append("authorId", this.getAuthorId()) //$NON-NLS-1$
                .append("label", this.getLabel()) //$NON-NLS-1$
                .append("sequencePosition", this.getSequencePosition()) //$NON-NLS-1$
                .append("activatorId", this.getActivatorId()) //$NON-NLS-1$
                .append("isActivated", this.getIsActivated()) //$NON-NLS-1$
                .append("creationDate", this.getCreationDate()) //$NON-NLS-1$
                .append("lastActionDate", this.getLastActionDate()) //$NON-NLS-1$
                .append("modificationDate", this.getModificationDate()) //$NON-NLS-1$
                .append("startTime", this.getStartTime()) //$NON-NLS-1$
                .append("endTime", this.getEndTime()) //$NON-NLS-1$
                .toString();
    }

}
