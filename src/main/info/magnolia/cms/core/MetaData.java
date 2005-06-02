/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;


/**
 *
 */
public class MetaData {

    /**
     * Top level atoms viewed as metadata of the specified content these must be set by the authoring system itself, but
     * could be changed via custom templates if neccessary.
     */
    public static final String TITLE = "title";

    public static final String CREATION_DATE = "creationdate";

    public static final String LAST_MODIFIED = "lastmodified";

    public static final String LAST_ACTION = "lastaction";

    public static final String AUTHOR_ID = "authorid";

    public static final String ACTIVATOR_ID = "activatorid";

    public static final String START_TIME = "starttime";

    public static final String END_TIME = "endtime";

    public static final String TEMPLATE = "template";

    public static final String TEMPLATE_TYPE = "templatetype";

    public static final String ACTIVATED = "activated";

    public static final String SEQUENCE_POS = "sequenceposition";

    public static final String ACTIVATION_INFO = ".activationInfo";

    public static final String DEFAULT_META_NODE = "MetaData";

    public static final long SEQUENCE_POS_COEFFICIENT = 1000;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MetaData.class);

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
        this(workingNode, DEFAULT_META_NODE, manager);
    }

    /**
     * constructor
     * @param workingNode current <code>Node</code> on which <code>MetaData</code> is requested
     * @param nodeName under which this data is saved
     */
    MetaData(Node workingNode, String nodeName, AccessManager manager) {
        this.setMetaNode(workingNode, nodeName);
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
     * this will be created even if the current user does not have write access on the content meta data is neded by the
     * system at any level
     */
    private void setMetaNode(Node workingNode, String name) {
        try {
            this.node = workingNode.getNode(name).getNode(ItemType.JCR_CONTENT.getSystemName());
        }
        catch (PathNotFoundException e) {
            try {
                this.node = workingNode.addNode(name, ItemType.NT_FILE);
                this.node = this.node.addNode(ItemType.JCR_CONTENT.getSystemName(), "nt:unstructured");
                this.node.setProperty("Data", name);
            }
            catch (ConstraintViolationException cve) {
                log.debug("Unable to create meta data node - " + name);
                log.debug(cve.getMessage());
            }
            catch (RepositoryException re) {
                log.error("Failed to create meta data node - " + name);
                log.error(re.getMessage(), re);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

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
            log.debug("Meta Data has not beed created");
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Part of metadata , could be used as html header.
     * @return String value of the requested metadata
     */
    public String getTitle() {
        return getStringProperty(TITLE);
    }

    /**
     * Part of metadata, could be used as html header.
     * @param value
     */
    public void setTitle(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(TITLE, value);
    }

    /**
     * Part of metadata, adds creation date of the current node
     */
    public void setCreationDate() throws AccessDeniedException {
        allowUpdate();
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(CREATION_DATE, value);
    }

    /**
     * Part of metadata, get creation date of the current node.
     * @return Calendar
     */
    public Calendar getCreationDate() {
        return this.getDateProperty(CREATION_DATE);
    }

    /**
     * Part of metadata, adds sequence number of the current node
     */
    public void setSequencePosition(long seqPos) throws AccessDeniedException {
        allowUpdate();
        if (seqPos == 0) {
            Date now = new Date();
            seqPos = now.getTime() * SEQUENCE_POS_COEFFICIENT;
        }
        try {
            this.node.getProperty(SEQUENCE_POS).setValue(seqPos);
        }
        catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(SEQUENCE_POS, seqPos);
            }
            catch (RepositoryException e) {
            }
        }
        catch (RepositoryException re) {
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot set property - " + SEQUENCE_POS);
            }
        }
    }

    /**
     * Part of metadata, adds sequence number of the current node
     */
    public void setSequencePosition() throws AccessDeniedException {
        setSequencePosition(0);
    }

    /**
     * Part of metadata, get sequence position of the current node
     * @return long
     */
    public long getSequencePosition() {
        try {
            return this.node.getProperty(SEQUENCE_POS).getLong();
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
                log.debug("MedaData has not been created");
                log.debug("cannot get property - " + SEQUENCE_POS);
            }
        }
        return 0;
    }

    /**
     * Part of metadata, adds activated status of the current node
     */
    public void setActivated() throws AccessDeniedException {
        allowUpdate();
        setProperty(ACTIVATED, true);
    }

    /**
     * Part of metadata, adds activated status of the current node.
     */
    public void setUnActivated() throws AccessDeniedException {
        allowUpdate();
        setProperty(ACTIVATED, false);
    }

    /**
     * Part of metadata, get last activated status of the current node
     * @return Calendar
     */
    public boolean getIsActivated() {
        return getBooleanProperty(ACTIVATED);
    }

    /**
     * Part of metadata, adds activated date of the current node
     */
    public void setLastActivationActionDate() throws AccessDeniedException {
        allowUpdate();
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(LAST_ACTION, value);
    }

    /**
     * Part of metadata, get last activated/de- date of the current node
     * @return Calendar
     */
    public Calendar getLastActionDate() {
        return getDateProperty(LAST_ACTION);
    }

    /**
     * Part of metadata, adds modification date of the current node
     */
    public void setModificationDate() throws AccessDeniedException {
        allowUpdate();
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(LAST_MODIFIED, value);
    }

    /**
     * Part of metadata, get last modified date of the current node
     * @return Calendar
     */
    public Calendar getModificationDate() {
        return getDateProperty(LAST_MODIFIED);
    }

    /**
     * Part of metadata, last known author of this node.
     * @return String value of the requested metadata
     */
    public String getAuthorId() {
        return getStringProperty(AUTHOR_ID);
    }

    /**
     * Part of metadata, current logged-in author who did some action on this page.
     * @param value
     */
    public void setAuthorId(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(AUTHOR_ID, value);
    }

    /**
     * Part of metadata, last known activator of this node.
     * @return String value of the requested metadata
     */
    public String getActivatorId() {
        return getStringProperty(ACTIVATOR_ID);
    }

    /**
     * Part of metadata, current logged-in author who last activated this page
     * @param value
     */
    public void setActivatorId(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(ACTIVATOR_ID, value);
    }

    /**
     * Part of metadata, node activation time
     */
    public void setStartTime(Calendar value) throws AccessDeniedException {
        allowUpdate();
        setProperty(START_TIME, value);
    }

    /**
     * Part of metadata, node activation time
     * @return Calendar
     */
    public Calendar getStartTime() {
        return getDateProperty(START_TIME);
    }

    /**
     * Part of metadata, node de-activation time
     */
    public void setEndTime(Calendar value) throws AccessDeniedException {
        allowUpdate();
        setProperty(END_TIME, value);
    }

    /**
     * Part of metadata, node de-activation time
     * @return Calendar
     */
    public Calendar getEndTime() {
        return getDateProperty(END_TIME);
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     * @return String value of the requested metadata
     */
    public String getTemplate() {
        return getStringProperty(TEMPLATE);
    }

    /**
     * Part of metadata, template which will be used to render content of this node
     * @param value
     */
    public void setTemplate(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(TEMPLATE, value);
    }

    /**
     * Part of metadata, template type : JSP - Servlet - _xxx_
     * @param value
     */
    public void setTemplateType(String value) throws AccessDeniedException {
        allowUpdate();
        setProperty(TEMPLATE_TYPE, value);
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, String value) throws AccessDeniedException {
        allowUpdate();
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
                log.debug("MedaData has not been created");
                log.debug("cannot set property - " + name);
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, long value) throws AccessDeniedException {
        allowUpdate();
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re);
            }
        }
        catch (RepositoryException re) {
            log.error(re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot set property - " + name);
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, double value) throws AccessDeniedException {
        allowUpdate();
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re);
            }
        }
        catch (RepositoryException re) {
            log.error(re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot set property - " + name);
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, boolean value) throws AccessDeniedException {
        allowUpdate();
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re);
            }
        }
        catch (RepositoryException re) {
            log.error(re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot set property - " + name);
            }
        }
    }

    /**
     * @param name
     * @param value
     */
    public void setProperty(String name, Calendar value) throws AccessDeniedException {
        allowUpdate();
        try {
            this.node.getProperty(name).setValue(value);
        }
        catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name, value);
            }
            catch (RepositoryException re) {
                log.error(re);
            }
        }
        catch (RepositoryException re) {
            log.error(re);
            throw new AccessDeniedException(re.getMessage());
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot set property - " + name);
            }
        }
    }

    /**
     * @param propertyName
     */
    public Calendar getDateProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getDate();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + propertyName + "] in node " + this.node);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot get property - " + propertyName);
            }
        }
        return null;
    }

    /**
     * @param propertyName
     */
    public boolean getBooleanProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getBoolean();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + propertyName + "] in node " + this.node);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot get property - " + propertyName);
            }
        }
        return false;
    }

    /**
     * @param propertyName
     */
    public double getDoubleProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getDouble();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + propertyName + "] in node " + this.node);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot get property - " + propertyName);
            }
        }
        return 0d;
    }

    /**
     * @param propertyName
     */
    public long getLongProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getLong();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + propertyName + "] in node " + this.node);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot get property - " + propertyName);
            }
        }
        return 0L;
    }

    /**
     * Returns a String property. If the property does not exist, this will return an empty String.
     * @param propertyName property name
     * @return the property value, never null
     */
    public String getStringProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getString();
        }
        catch (PathNotFoundException re) {
            if (log.isDebugEnabled()) {
                log.debug("PathNotFoundException for property [" + propertyName + "] in node " + this.node);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug("MedaData has not been created");
                log.debug("cannot get property - " + propertyName);
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append("title", this.getTitle())
            .append("template", this.getTemplate())
            .append("authorId", this.getAuthorId())
            .append("label", this.getLabel())
            .append("sequencePosition", this.getSequencePosition())
            .append("activatorId", this.getActivatorId())
            .append("isActivated", this.getIsActivated())
            .append("creationDate", this.getCreationDate())
            .append("lastActionDate", this.getLastActionDate())
            .append("modificationDate", this.getModificationDate())
            .append("startTime", this.getStartTime())
            .append("endTime", this.getEndTime())
            .toString();
    }

}
