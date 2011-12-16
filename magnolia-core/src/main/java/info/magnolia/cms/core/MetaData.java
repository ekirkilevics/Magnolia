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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meta data of a content like creation date, modification date, assigned template, ...
 *
 * @version $Id$
 */
public class MetaData {
    private static final Logger log = LoggerFactory.getLogger(MetaData.class);

    /**
     * Top level atoms viewed as metadata of the specified content these must be set by the authoring system itself, but
     * could be changed via custom templates if necessary.
     */
    public static final String TITLE = "title";

    public static final String CREATION_DATE = "creationdate";

    public static final String LAST_MODIFIED = "lastmodified";

    public static final String LAST_ACTION = "lastaction";

    public static final String AUTHOR_ID = "authorid";

    public static final String ACTIVATOR_ID = "activatorid";

    public static final String TEMPLATE = "template";

    public static final String TEMPLATE_TYPE = "templatetype";

    public static final String ACTIVATED = "activated";

    /**
     * Name of the Node hosting the MetaData.
     */
    public static final String DEFAULT_META_NODE = "MetaData";

    public static final int ACTIVATION_STATUS_NOT_ACTIVATED = 0;

    public static final int ACTIVATION_STATUS_MODIFIED = 1;

    public static final int ACTIVATION_STATUS_ACTIVATED = 2;

    /**
     * meta data node.
     */
    private Node node;

    /**
     * @param workingNode
     *            current <code>Node</code> on which <code>MetaData</code> is requested
     * @param ignoredAccessManager
     *            no longer required hence use other constructor.
     *
     * @deprecated since 4.5 use MetaData(Node) instead.
     */
    protected MetaData(Node workingNode, AccessManager ignoredAccessManager) {
        this(workingNode);
    }

    /**
     * @param workingNode
     *            current <code>Node</code> on which <code>MetaData</code> is requested
     */
    public MetaData(Node workingNode) {
        try {
            this.node = workingNode.getNode(DEFAULT_META_NODE);
        } catch (PathNotFoundException e) {
            try {
                log.debug("{} does not support MetaData, check node type definition of {}", workingNode.getPath(),
                        workingNode.getPrimaryNodeType().getName());
            } catch (RepositoryException re) {
                // should never come here
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

    public String getHandle() throws RepositoryException {
        return this.node.getPath();
    }

    /**
     * Part of metadata, same as name of actual storage node. This value is unique at the hierarchy level context.
     *
     * @return String value of the requested metadata
     */
    public String getLabel() {
        if (node == null) {
            if (log.isDebugEnabled()) {
                log.debug("MetaData has not been created or this node does not support MetaData");
            }
        } else {
            try {
                return this.node.getName();
            } catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * get property name with the prefix.
     *
     * @return name with namespace prefix
     */
    private String getInternalPropertyName(String name) {
        if (StringUtils.indexOf(name, RepositoryConstants.NAMESPACE_PREFIX + ":") != 0) {
            return RepositoryConstants.NAMESPACE_PREFIX + ":" + name;
        }
        return name;
    }

    /**
     * Part of metadata , could be used as html header.
     *
     * @return String value of the requested metadata
     */
    public String getTitle() {
        return getStringProperty(TITLE);
    }

    /**
     * Part of metadata, could be used as html header.
     *
     * @param value
     */
    public void setTitle(String value) {
        setProperty(TITLE, value);
    }

    /**
     * Part of metadata, adds creation date of the current node.
     */
    public void setCreationDate() {
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(CREATION_DATE, value);
    }

    /**
     * Part of metadata, get creation date of the current node.
     *
     * @return Calendar
     */
    public Calendar getCreationDate() {
        return this.getDateProperty(CREATION_DATE);
    }

    /**
     * Part of metadata, adds activated status of the current node.
     */
    public void setActivated() {
        setProperty(ACTIVATED, true);
    }

    /**
     * Part of metadata, adds activated status of the current node.
     */
    public void setUnActivated() {
        setProperty(ACTIVATED, false);
    }

    /**
     * Part of metadata, get last activated status of the current node.
     *
     * @return Calendar
     */
    public boolean getIsActivated() {
        return getBooleanProperty(ACTIVATED);
    }

    /**
     * Returns one of the ACTIVATION_STATUS_* constants.
     */
    public int getActivationStatus() {
        if (getIsActivated()) {
            if (getModificationDate() != null && getModificationDate().after(getLastActionDate())) {
                // node has been modified after last activation
                return ACTIVATION_STATUS_MODIFIED;
            }
            // activated and not modified ever since
            return ACTIVATION_STATUS_ACTIVATED;
        }
        // never activated or deactivated
        return ACTIVATION_STATUS_NOT_ACTIVATED;
    }

    /**
     * Part of metadata, adds activated date of the current node.
     */
    public void setLastActivationActionDate() {
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(LAST_ACTION, value);
    }

    /**
     * Part of metadata, get last activated/de- date of the current node.
     *
     * @return Calendar
     */
    public Calendar getLastActionDate() {
        return getDateProperty(LAST_ACTION);
    }

    /**
     * Part of metadata, adds modification date of the current node.
     */
    public void setModificationDate() {
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(LAST_MODIFIED, value);
    }

    /**
     * Get last modified date of the node to which this meta data belongs or creation date in case content was not
     * modified since.
     *
     * @return Calendar when last modification date can't be found.
     */
    public Calendar getModificationDate() {
        Calendar modDate = getDateProperty(LAST_MODIFIED);
        if (modDate == null) {
            modDate = getCreationDate();
        }
        return modDate;
    }

    /**
     * Part of metadata, last known author of this node.
     *
     * @return String value of the requested metadata
     */
    public String getAuthorId() {
        return getStringProperty(AUTHOR_ID);
    }

    /**
     * Part of metadata, current logged-in author who did some action on this page.
     *
     * @param value
     */
    public void setAuthorId(String value) {
        setProperty(AUTHOR_ID, value);
    }

    /**
     * Part of metadata, last known activator of this node.
     *
     * @return String value of the requested metadata
     */
    public String getActivatorId() {
        return getStringProperty(ACTIVATOR_ID);
    }

    /**
     * Part of metadata, current logged-in author who last activated this page.
     *
     * @param value
     */
    public void setActivatorId(String value) {
        setProperty(ACTIVATOR_ID, value);
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     *
     * @return String value of the requested metadata
     */
    public String getTemplate() {
        return getStringProperty(TEMPLATE);
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     *
     * @param value
     */
    public void setTemplate(String value) {
        setProperty(TEMPLATE, value);
    }

    public void setProperty(String name, String value) {
        setJCRProperty(name, value);
    }

    public void setProperty(String name, long value) {
        setJCRProperty(name, value);
    }

    public void setProperty(String name, double value) {
        setJCRProperty(name, value);
    }

    public void setProperty(String name, boolean value) {
        setJCRProperty(name, value);
    }

    public void setProperty(String name, Calendar value) {
        setJCRProperty(name, value);
    }

    private void setJCRProperty(String name, Object value) {
        final String propName = this.getInternalPropertyName(name);
        if (node == null) {
            log.debug("MetaData has not been created or this node does not support MetaData. Cannot set property {}",
                    propName);
        } else {
            try {
                PropertyUtil.setProperty(node, propName, value);
            } catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
    }

    public boolean getBooleanProperty(String name) {
        try {
            final Property property = getJCRProperty(name);
            if (property != null) {
                return property.getBoolean();
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
    }

    public double getDoubleProperty(String name) {
        try {
            final Property property = getJCRProperty(name);
            if (property != null) {
                return property.getDouble();
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return 0d;
    }

    public long getLongProperty(String name) {
        try {
            final Property property = getJCRProperty(name);
            if (property != null) {
                return property.getLong();
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return 0L;
    }

    public String getStringProperty(String name) {
        try {
            final Property property = getJCRProperty(name);
            if (property != null) {
                return property.getString();
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return StringUtils.EMPTY;
    }

    public Calendar getDateProperty(String name) {
        try {
            final Property property = getJCRProperty(name);
            if (property != null) {
                return property.getDate();
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return null;
    }

    /**
     * remove specified property.
     *
     * @param name
     *            of the property to be removed
     * @throws PathNotFoundException
     *             if property does not exist
     * @throws RepositoryException
     *             if unable to remove
     */
    public void removeProperty(String name) throws PathNotFoundException, RepositoryException {
        this.node.getProperty(this.getInternalPropertyName(name)).remove();
    }

    private Property getJCRProperty(String name) throws RepositoryException {
        final String propName = this.getInternalPropertyName(name);
        if (node == null) {
            log.debug("MetaData has not been created or this node does not support MetaData. Cannot set property {}",
                    propName);
        } else {
            try {
                return node.getProperty(propName);
            } catch (PathNotFoundException re) {
                log.debug("PathNotFoundException for property [{}] in node {}", propName, node);
            }
        }
        return null;
    }

    /**
     * check if property exists.
     *
     * @param name
     * @return true if the specified property exist
     *
     * @deprecated since 4.0 - not used
     */
    @Deprecated
    public boolean hasProperty(String name) {
        try {
            return this.node.hasProperty(this.getInternalPropertyName(name));
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("title", this.getTitle()).append("template", this.getTemplate())
                .append("authorId", this.getAuthorId()).append("label", this.getLabel())
                .append("activatorId", this.getActivatorId()).append("isActivated", this.getIsActivated())
                .append("creationDate", this.getCreationDate()).append("lastActionDate", this.getLastActionDate())
                .append("modificationDate", this.getModificationDate()).toString();
    }
}
