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
package info.magnolia.cms.exchange;

import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.util.NodeTypes;

/**
 * Utility class for setting and querying activation meta data on nodes. Used for nodes having the
 * <code>mgnl:activatable</code> mixin.
 */
public class ActivationUtil {

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

        Calendar lastModified = NodeTypes.LastModifiedMixin.getLastModified(node);
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
        return node.hasProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS) && node.getProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS).getBoolean();
    }

    /**
     * Flags the node as activated.
     */
    public static void setActivated(Node node) throws RepositoryException {
        node.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, true);
    }

    /**
     * Flags the node has not activated.
     */
    public static void setUnactivated(Node node) throws RepositoryException {
        node.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, false);
    }

    /**
     * Returns the date when the node was last activated or null if no activation date has been stored on the node.
     */
    public static Calendar getLastActivated(Node node) throws RepositoryException {
        return node.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED) ? node.getProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED).getDate() : null;
    }

    /**
     * Returns the name of the user that last activated the node or null if no activating user has been stored on the node.
     */
    public static String getLastActivatedBy(Node node) throws RepositoryException {
        return node.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY) ? node.getProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY).getString() : null;
    }

    /**
     * Sets the time when the node was most recently activated.
     */
    public static void setLastActivated(Node node) throws RepositoryException {
        node.setProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED, Calendar.getInstance());
    }

    /**
     * Sets the name of the user that performed the most recent activation.
     */
    public static void setLastActivatedBy(Node node, String userName) throws RepositoryException {
        node.setProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY, userName);
    }
}
