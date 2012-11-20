/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr.util;

import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Magnolia defined NodeTypes together with their properties and some convenience methods.
 */
public class NodeTypes {

    /** Namespace for Magnolia extensions. */
    public static final String MGNL_PREFIX = "mgnl:";

    /** Namespace for jcr properties. */
    public static final String JCR_PREFIX = "jcr:";

    /** Default suffix for userName keeping properties. */
    public static final String BY = "By";

    /**
     * Represents the mixin mgnl:lastModified.
     */
    public static class LastModifiedMixin {
        public static final String NAME = MGNL_PREFIX + "lastModified";
        public static final String LAST_MODIFIED = NAME;
        public static final String LAST_MODIFIED_BY = LAST_MODIFIED + BY;

        /**
         * Returns the date when this node was last modified. If the no modification date has been stored on the node this
         * method return the creation date if set, otherwise null is returned.
         */
        public static Calendar getLastModified(Node node) throws RepositoryException {
            return node.hasProperty(LAST_MODIFIED) ? node.getProperty(LAST_MODIFIED).getDate() : CreatedMixin.getCreated(node);
        }

        /**
         * Returns the name of the user that last modified the node. If no modification has been stored on the node
         * this method return the name of the user that created the node if set, otherwise null is returned.
         */
        public static String getLastModifiedBy(Node node) throws RepositoryException {
            return node.hasProperty(LAST_MODIFIED_BY) ? node.getProperty(LAST_MODIFIED_BY).getString() : CreatedMixin.getCreatedBy(node);
        }

        /**
         * Sets the date of modification and uses {@link info.magnolia.context.MgnlContext} to set the name of the user modifying a node.
         */
        public static void updateModification(Node node) throws RepositoryException {
            updateModification(node, MgnlContext.getUser().getName());
        }

        /**
         * Sets the current date as date of modification and the name of the user modifying a node.
         */
        static void updateModification(Node node, String userName) throws RepositoryException {
            updateModification(node, userName, Calendar.getInstance());
        }

        /**
         * Sets the date of modification and the name of the user modifying a node.
         */
        static void updateModification(Node node, String userName, Calendar lastModified) throws RepositoryException {
            node.setProperty(LAST_MODIFIED, lastModified);
            node.setProperty(LAST_MODIFIED_BY, userName);
            AuditLoggingUtil.log(AuditLoggingUtil.ACTION_MODIFY, node.getSession().getWorkspace().getName(), node
                    .getPrimaryNodeType().getName(), node.getName());
        }

        /**
         * Sets the current date as date of modification for a node.
         */
        public static void setLastModified(Node node) throws RepositoryException {
            setLastModified(node, Calendar.getInstance());
        }

        /**
         * Sets the date of modification for a node.
         */
        public static void setLastModified(Node node, Calendar modified) throws RepositoryException {
            node.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED, modified);
        }

        /**
         * Sets the name of the user that last modified a node.
         */
        public static void setLastModifiedBy(Node node, String userName) throws RepositoryException {
            node.setProperty(LAST_MODIFIED_BY, userName);
        }
    }

    /**
     * Represents the mixin mgnl:activatable.
     */
    public static class ActivatableMixin {
        public static final String NAME = MGNL_PREFIX + "activatable";
        public static final String LAST_ACTIVATED = MGNL_PREFIX + "lastActivated";
        public static final String LAST_ACTIVATED_BY = LAST_ACTIVATED + BY;
        public static final String ACTIVATION_STATUS = MGNL_PREFIX + "activationStatus";

        public static final int ACTIVATION_STATUS_NOT_ACTIVATED = 0;

        public static final int ACTIVATION_STATUS_MODIFIED = 1;

        public static final int ACTIVATION_STATUS_ACTIVATED = 2;

        /**
         * Returns the activation status of the node. Returns one of the constants:
         * <ul>
         * <li>{@link #ACTIVATION_STATUS_NOT_ACTIVATED} if the node has not been activated</li>
         * <li>{@link #ACTIVATION_STATUS_MODIFIED} has been activated and subsequently modified</li>
         * <li>{@link #ACTIVATION_STATUS_ACTIVATED} has been activated and not modified since</li>
         * </ul>
         */
        public static int getActivationStatus(Node node) throws RepositoryException {

            if (!isActivated(node)) {
                // never activated or deactivated
                return ACTIVATION_STATUS_NOT_ACTIVATED;
            }

            Calendar lastModified = LastModifiedMixin.getLastModified(node);
            Calendar lastActivated = getLastActivated(node);

            if (lastModified != null && lastModified.after(lastActivated)) {
                // node has been modified after last activation
                return ACTIVATION_STATUS_MODIFIED;
            }

            // activated and not modified ever since
            return ACTIVATION_STATUS_ACTIVATED;
        }

        /**
         * Returns true if the node has been activated.
         */
        public static boolean isActivated(Node node) throws RepositoryException {
            return node.hasProperty(ACTIVATION_STATUS) && node.getProperty(ACTIVATION_STATUS).getBoolean();
        }

        /**
         * Set whether the node is activated or not.
         */
        public static void setActivated(Node node, boolean isActivated) throws RepositoryException {
            node.setProperty(ACTIVATION_STATUS, isActivated);
        }

        /**
         * Returns the date when the node was last activated or null if no activation date has been stored on the node.
         */
        public static Calendar getLastActivated(Node node) throws RepositoryException {
            return node.hasProperty(LAST_ACTIVATED) ? node.getProperty(LAST_ACTIVATED).getDate() : null;
        }

        /**
         * Returns the name of the user that last activated the node or null if no activating user has been stored on the node.
         */
        public static String getLastActivatedBy(Node node) throws RepositoryException {
            return node.hasProperty(LAST_ACTIVATED_BY) ? node.getProperty(LAST_ACTIVATED_BY).getString() : null;
        }

        /**
         * Sets the time when the node was most recently activated.
         */
        public static void setLastActivated(Node node) throws RepositoryException {
            node.setProperty(LAST_ACTIVATED, Calendar.getInstance());
        }

        /**
         * Sets the name of the user that performed the most recent activation.
         */
        public static void setLastActivatedBy(Node node, String userName) throws RepositoryException {
            node.setProperty(LAST_ACTIVATED_BY, userName);
        }

    }

    /**
     * Represents the mixin mgnl:created.
     */
    public static class CreatedMixin {
        public static final String NAME = MGNL_PREFIX + "created";
        public static final String CREATED = NAME;
        public static final String CREATED_BY = CREATED + BY;

        /**
         * Returns the creation date of a node or null if creation date isn't set.
         */
        public static Calendar getCreated(Node node) throws RepositoryException {
            return node.hasProperty(NodeTypes.CreatedMixin.CREATED) ? node.getProperty(NodeTypes.CreatedMixin.CREATED).getDate() : null;
        }

        /**
         * Returns the name of the user that created a node.
         */
        public static String getCreatedBy(Node node) throws RepositoryException {
            return node.hasProperty(NodeTypes.CreatedMixin.CREATED_BY) ? node.getProperty(NodeTypes.CreatedMixin.CREATED_BY).getString() : null;
        }

        /**
         * Sets the current date as the node's creation date and uses {@link info.magnolia.context.MgnlContext} to set the name of the creating
         * user. Used with nodes having the <code>mgnl:created</code> mixin.
         */
        public static void setCreation(Node node) throws RepositoryException {
            setCreation(node, MgnlContext.getUser().getName());
        }

        /**
         * Sets the current date as the node's creation date and sets the name of the creating user. Also sets the date of
         * modification and the user last having modified the node to the same values. Used with nodes having the
         * <code>mgnl:created</code> mixin.
         */
        public static void setCreation(Node node, String userName) throws RepositoryException {
            setCreation(node, userName, new GregorianCalendar(TimeZone.getDefault()));
        }

        /**
         * Sets the supplied date as the node's creation date and sets the name of the creating user. Also sets the date of
         * modification and the user last having modified the node to the same values. Used with nodes having the
         * <code>mgnl:created</code> mixin.
         */
        public static void setCreation(Node node, String userName, Calendar created) throws RepositoryException {
            node.setProperty(NodeTypes.CreatedMixin.CREATED, created);
            node.setProperty(NodeTypes.CreatedMixin.CREATED_BY, userName);
            node.setProperty(LastModifiedMixin.LAST_MODIFIED, created);
            node.setProperty(LastModifiedMixin.LAST_MODIFIED_BY, userName);
        }

        /**
         * Sets the current date as the node's creation date. Used with nodes having the <code>mgnl:created</code> mixin.
         */
        public void setCreated(Node node) throws RepositoryException {
            node.setProperty(NodeTypes.CreatedMixin.CREATED, new GregorianCalendar(TimeZone.getDefault()));
        }
    }

    /**
     * Represents the mixin mgnl:renderable.
     */
    public static class RenderableMixin {
        public static final String NAME = MGNL_PREFIX + "renderable";
        public static final String TEMPLATE = MGNL_PREFIX + "template";

        /**
         * Returns the template assigned to the node or null of none has been assigned. Used with nodes having the
         * <code>mgnl:renderable</code> mixin.
         */
        public static String getTemplate(Node node) throws RepositoryException {
            return node.hasProperty(TEMPLATE) ? node.getProperty(TEMPLATE).getString() : null;
        }

        /**
         * Sets the template assigned to the node. Used with nodes having the <code>mgnl:renderable</code> mixin.
         */
        public static void setTemplate(Node node, String template) throws RepositoryException {
            node.setProperty(TEMPLATE, template);
        }
    }

    /**
     * Represents the mixin mgnl:deleted.
     */
    public static class DeletedMixin {
        public static final String NAME = MGNL_PREFIX + "deleted";
        public static final String DELETED = NAME;
        public static final String DELETED_BY = DELETED + BY;
    }

    /**
     * Represents the mixin mgnl:versionable.
     */
    public static class VersionableMixin {
        public static final String NAME = MGNL_PREFIX + "versionable";
        public static final String COMMENT = MGNL_PREFIX + "comment";
    }

    /**
     * Represents the nodeType mgnl:folder.
     */
    public static class Folder {
        public static final String NAME = MGNL_PREFIX + "folder";
    }

    /**
     * Represents the nodeType mgnl:resource.
     */
    public static class Resource {
        public static final String NAME = MGNL_PREFIX + "resource";
    }

    /**
     * Represents the nodeType mgnl:content.
     */
    public static class Content {
        public static final String NAME = MGNL_PREFIX + "content";
    }

    /**
     * Represents the nodeType mgnl:contentNode.
     */
    public static class ContentNode {
        public static final String NAME = MGNL_PREFIX + "contentNode";
    }

    /**
     * Represents the nodeType mgnl:nodeData.
     */
    public static class NodeData {
        public static final String NAME = MGNL_PREFIX + "nodeData";
    }

    /**
     * Represents the nodeType mgnl:page.
     */
    public static class Page {
        public static final String NAME = MGNL_PREFIX + "page";
    }

    /**
     * Represents the nodeType mgnl:area.
     */
    public static class Area {
        public static final String NAME = MGNL_PREFIX + "area";
    }

    /**
     * Represents the nodeType mgnl:component.
     */
    public static class Component {
        public static final String NAME = MGNL_PREFIX + "component";
    }

    /**
     * Represents the nodeType mgnl:user.
     */
    public static class User {
        public static final String NAME = MGNL_PREFIX + "user";
    }

    /**
     * Represents the nodeType mgnl:role.
     */
    public static class Role {
        public static final String NAME = MGNL_PREFIX + "role";
    }

    /**
     * Represents the nodeType mgnl:group.
     */
    public static class Group {
        public static final String NAME = MGNL_PREFIX + "group";
    }

    /**
     * Represents the nodeType mgnl:reserve.
     */
    public static class System {
        public static final String NAME = MGNL_PREFIX + "reserve";
    }
}
