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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Magnolia NodeTypes,
 */
public class NodeTypes {

    /** Namespace for Magnolia extensions */
    public static final String MGNL_PREFIX = "mgnl:";

    /** Default suffix for userName keeping properties */
    public static final String BY = "By";

    public static class LastModifiedMixin {
        public static final String LAST_MODIFIED = MGNL_PREFIX + "lastModified";
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
        public static void updateModification(Node node, String userName) throws RepositoryException {
            updateModification(node, userName, Calendar.getInstance());
        }

        /**
         * Sets the date of modification and the name of the user modifying a node.
         */
        public static void updateModification(Node node, String userName, Calendar lastModified) throws RepositoryException {
            node.setProperty(LAST_MODIFIED, lastModified);
            node.setProperty(LAST_MODIFIED_BY, userName);
        }

        /**
         * Sets the date of modification for a node.
         */
        public static void setLastModified(Node node) throws RepositoryException {
            node.setProperty(LAST_MODIFIED, Calendar.getInstance());
        }

        /**
         * Sets the name of the user that last modified a node.
         */
        public static void setLastModifiedBy(Node node, String userName) throws RepositoryException {
            node.setProperty(LAST_MODIFIED_BY, userName);
        }
    }
    
    public static class ActivatableMixin {
        public static final String LAST_ACTIVATED = MGNL_PREFIX + "lastActivated";
        public static final String LAST_ACTIVATED_BY = LAST_ACTIVATED + BY;
        public static final String ACTIVATION_STATUS = MGNL_PREFIX + "activationStatus";
    }

    public static class CreatedMixin {
        public static final String CREATED = MGNL_PREFIX + "created";
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

    public static class RenderableMixin {
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

    public static class DeletedMixin {
        public static final String DELETED = MGNL_PREFIX + "deleted";
        public static final String DELETED_BY = DELETED + BY;
    }
}
