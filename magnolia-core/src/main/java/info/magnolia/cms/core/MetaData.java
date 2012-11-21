/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the meta data of a node, its creation date, modification date, assigned template etc.
 *
 * As of 5.0 the meta data is stored directly on the node itself using mixins rather than in a subnode named MetaData.
 * With this change this class was deprecated and replaced with corresponding methods in
 * {@link info.magnolia.jcr.util.NodeUtil}.
 *
 * @deprecated since 5.0 - use instead the corresponding methods in NodeUtil
 */
public class MetaData {
    private static final Logger log = LoggerFactory.getLogger(MetaData.class);

    /**
     * Top level atoms viewed as metadata of the specified content these must be set by the authoring system itself, but
     * could be changed via custom templates if necessary.
     */

    /**
     * @deprecated since 5.0 - no longer supported
     */
    public static final String TITLE = "title";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.CreatedMixin#CREATED} instead
     */
    public static final String CREATION_DATE = "creationdate";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.LastModifiedMixin#LAST_MODIFIED} instead
     */
    public static final String LAST_MODIFIED = "lastmodified";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#LAST_ACTIVATED} instead
     */
    public static final String LAST_ACTION = "lastaction";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.LastModifiedMixin#LAST_MODIFIED_BY} instead
     */
    public static final String AUTHOR_ID = "authorid";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#LAST_ACTIVATED_BY} instead
     */
    public static final String ACTIVATOR_ID = "activatorid";

    /**
     * Template assigned to the node.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.RenderableMixin#TEMPLATE} instead
     */
    public static final String TEMPLATE = "template";

    /**
     * @deprecated since 5.0 - no longer supported
     */
    public static final String TEMPLATE_TYPE = "templatetype";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#ACTIVATION_STATUS} instead
     */
    public static final String ACTIVATED = "activated";

    /**
     * Name of the node hosting the MetaData.
     *
     * @deprecated since 5.0 - there's no longer such a subnode
     */
    public static final String DEFAULT_META_NODE = "MetaData";

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#ACTIVATION_STATUS_NOT_ACTIVATED} instead
     */
    public static final int ACTIVATION_STATUS_NOT_ACTIVATED = NodeTypes.ActivatableMixin.ACTIVATION_STATUS_NOT_ACTIVATED;

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#ACTIVATION_STATUS_MODIFIED} instead
     */
    public static final int ACTIVATION_STATUS_MODIFIED = NodeTypes.ActivatableMixin.ACTIVATION_STATUS_MODIFIED;

    /**
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#ACTIVATION_STATUS_ACTIVATED} instead
     */
    public static final int ACTIVATION_STATUS_ACTIVATED = NodeTypes.ActivatableMixin.ACTIVATION_STATUS_ACTIVATED;

    /**
     * Since 5.0 this is the working node itself.
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
        this.node = workingNode;
    }

    /**
     * Maps property names from the names used when we had a MetaData sub node to their replacements on mixins on the
     * working node itself.
     */
    private static Map<String, String> propertyMappings = new ConcurrentHashMap<String, String>();

    static {
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + CREATION_DATE, NodeTypes.CreatedMixin.CREATED);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + LAST_MODIFIED, NodeTypes.LastModifiedMixin.LAST_MODIFIED);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + LAST_ACTION, NodeTypes.ActivatableMixin.LAST_ACTIVATED);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + AUTHOR_ID, NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + ACTIVATOR_ID, NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + TEMPLATE, NodeTypes.RenderableMixin.TEMPLATE);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":" + ACTIVATED, NodeTypes.ActivatableMixin.ACTIVATION_STATUS);
        propertyMappings.put(RepositoryConstants.NAMESPACE_PREFIX + ":comment", NodeTypes.VersionableMixin.COMMENT);
    }

    /**
     * Returns the property name to use including its prefix.
     *
     * @return name with namespace prefix
     */
    private String getInternalPropertyName(String name) {
        if (StringUtils.indexOf(name, ":") < 0) {
            name = RepositoryConstants.NAMESPACE_PREFIX + ":" + name;
        }

        String newName = propertyMappings.get(name);

        if (newName == null) {
            throw new IllegalArgumentException("Unsupported meta data property: " + name);
        }

        return newName;
    }

    /**
     * @return value of property TITLE if it's around on working node
     *
     * @deprecated since 5.0 - only for backwards compatibility.
     */
    public String getTitle() {
        return getStringProperty(TITLE);
    }

    /**
     * Will set value of property TITLE on working node.
     *
     * @deprecated since 5.0 - only for backwards compatibility.
     */
    public void setTitle(String value) {
        setProperty(TITLE, value);
    }

    /**
     * Part of metadata, adds creation date of the current node.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.CreatedMixin#setCreated(Node)}
     */
    public void setCreationDate() {
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(CREATION_DATE, value);
    }

    /**
     * Part of metadata, get creation date of the current node.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.CreatedMixin#getCreated(Node)}
     */
    public Calendar getCreationDate() {
        return this.getDateProperty(CREATION_DATE);
    }

    /**
     * Part of metadata, adds activated status of the current node.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#setActivated(javax.jcr.Node, boolean)}
     */
    public void setActivated() {
        setProperty(ACTIVATED, true);
    }

    /**
     * Part of metadata, adds activated status of the current node.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#setActivated(javax.jcr.Node, boolean)}
     */
    public void setUnActivated() {
        setProperty(ACTIVATED, false);
    }

    /**
     * Part of metadata, get last activated status of the current node.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#isActivated(javax.jcr.Node)}
     */
    public boolean getIsActivated() {
        return getBooleanProperty(ACTIVATED);
    }

    /**
     * Returns one of the ACTIVATION_STATUS_* constants.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#getActivationStatus(javax.jcr.Node)}
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
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#setLastActivated(javax.jcr.Node)}
     */
    public void setLastActivationActionDate() {
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(LAST_ACTION, value);
    }

    /**
     * Part of metadata, get last activated/de- date of the current node.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#getLastActivated(javax.jcr.Node)}
     */
    public Calendar getLastActionDate() {
        return getDateProperty(LAST_ACTION);
    }

    /**
     * Part of metadata, adds modification date of the current node.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.LastModifiedMixin#setLastModified(Node)}
     */
    public void setModificationDate() {
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        setProperty(LAST_MODIFIED, value);
    }

    /**
     * Get last modified date of the node to which this meta data belongs or creation date in case content was not
     * modified since.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.LastModifiedMixin#getLastModified(Node)}
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
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.LastModifiedMixin#getLastModifiedBy(javax.jcr.Node)}
     */
    public String getAuthorId() {
        return getStringProperty(AUTHOR_ID);
    }

    /**
     * Part of metadata, current logged-in author who did some action on this page.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.LastModifiedMixin#setLastModifiedBy(javax.jcr.Node, String)}
     */
    public void setAuthorId(String value) {
        setProperty(AUTHOR_ID, value);
    }

    /**
     * Part of metadata, last known activator of this node.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#getLastActivatedBy(javax.jcr.Node)}
     */
    public String getActivatorId() {
        return getStringProperty(ACTIVATOR_ID);
    }

    /**
     * Part of metadata, current logged-in author who last activated this page.
     *
     * @deprecated since 5.0 - use {@link NodeTypes.ActivatableMixin#setLastActivatedBy(javax.jcr.Node, String)}
     */
    public void setActivatorId(String value) {
        setProperty(ACTIVATOR_ID, value);
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.RenderableMixin#getTemplate(javax.jcr.Node)}
     */
    public String getTemplate() {
        return getStringProperty(TEMPLATE);
    }

    /**
     * Part of metadata, template which will be used to render content of this node.
     *
     * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeTypes.RenderableMixin#setTemplate(javax.jcr.Node, String)}
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
        try {
            PropertyUtil.setProperty(node, propName, value);
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
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
        try {
            return node.getProperty(propName);
        } catch (PathNotFoundException re) {
            log.debug("PathNotFoundException for property [{}] in node {}", propName, node);
        }
        return null;
    }
}
