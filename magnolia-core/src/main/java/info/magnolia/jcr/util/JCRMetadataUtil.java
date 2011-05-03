/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.MetaData;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingUtil;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.UnhandledException;

/**
 * Collection of utilities to simplify working with the JCR API. In contrast to info.magnolia.cms.core.Content it is -
 * from a caller perspective - independent from Content API. Internally content API is still used for now, but this will
 * most probably change quite soon.
 * 
 * @deprecated temporary
 */
public class JCRMetadataUtil {


    private static class JCRMetaData extends MetaData {

        public JCRMetaData(Node node) throws RepositoryException {
            // do not use AccessManager for now
            super(node, node.getSession());
        }
    }

    public static MetaData getMetaData(Node node) {
        try {
            return new JCRMetaData(node);
        } catch (RepositoryException e) {
            throw new UnhandledException(e);
        }
    }

    /**
     * Return iconFileName for a node.
     *
     * @param node node to read status from
     * @return file name for an icon
     */
    public static String getActivationStatusIcon(Node node) {

        MetaData metaData = getMetaData(node);
        String iconFileName;
        switch (metaData.getActivationStatus()) {
        case MetaData.ACTIVATION_STATUS_MODIFIED:
            iconFileName = "indicator_yellow.gif";
            break;
        case MetaData.ACTIVATION_STATUS_ACTIVATED:
            iconFileName = "indicator_green.gif";
            break;
        default:
            iconFileName = "indicator_red.gif";
        }

        return iconFileName;
    }

    public static void updateMetaData(Node node) throws RepositoryException {
        MetaData md = getMetaData(node);
        md.setModificationDate();
        md.setAuthorId(MgnlContext.getUser().getName());
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_MODIFY, node.getSession().getWorkspace().getName(), node
                .getPrimaryNodeType().getName(), node.getName());
    }


    /**
     * @return the lastModification or null it it was not set in JCR.
     */
    public static Calendar getLastModification(Node node) throws PathNotFoundException, RepositoryException, ValueFormatException {
        Node meta = node.getNode(MetaData.DEFAULT_META_NODE);
        String lastMod = ContentRepository.NAMESPACE_PREFIX + ":" + MetaData.LAST_MODIFIED;
        return (meta.hasProperty(lastMod)) ? meta.getProperty(lastMod).getDate() : null;
    }

}
