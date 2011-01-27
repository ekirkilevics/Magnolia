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
package info.magnolia.module.admincentral.jcr;

import info.magnolia.cms.core.MetaData;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;


/**
 * Collection of utilities to simplify working with the JCR API. To be checked how much this type
 * should be based on current MetaData-support implemented in the Content-API. Right now it
 * delegates partly to the "old" MetaData-support.
 *
 * @author daniellipp
 * @version $Id$
 */
public class JCRMetadataUtil {

    private static class JCRMetaData extends MetaData {
        protected JCRMetaData(Node node) {
            // do not use AccessManager for now
            super(node, null);
        }
    }

    public static final String ACTIVATED = MetaData.ACTIVATED;

    public static final String LAST_ACTION = MetaData.LAST_ACTION;

    public static final String LAST_MODIFIED = MetaData.LAST_MODIFIED;

    /**
     * Name of the childnode hosting the metadata.
     */
    public static final String META_DATA_NODE_NAME = "MetaData";

    public static final String TEMPLATE = MetaData.TEMPLATE;

    public static String getActivationStatusIcon(Node parent) {
        JCRMetaData metaData = new JCRMetaData(parent);
        String imgSrc;
        switch (metaData.getActivationStatus()) {
            case MetaData.ACTIVATION_STATUS_MODIFIED :
                imgSrc = "indicator_yellow.gif";
                break;
            case MetaData.ACTIVATION_STATUS_ACTIVATED :
                imgSrc = "indicator_green.gif";
                break;
            default :
                imgSrc = "indicator_red.gif";
        }
        return imgSrc;
    }

    public static Node getMetaData(Node parent) throws RepositoryException {
        return parent.getNode(META_DATA_NODE_NAME);
    }

    public static Property getMetaDataProperty(Node parent, String propertyId) throws RepositoryException {
        return getMetaData(parent).getProperty(propertyId);
    }
}
