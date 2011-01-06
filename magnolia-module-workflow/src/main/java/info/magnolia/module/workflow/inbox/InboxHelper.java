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
package info.magnolia.module.workflow.inbox;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utiities for {@link Inbox} and {@link SubPagesList}.
 *
 * @author philipp
 * @version $Id$
 */
public class InboxHelper {
    private static final Logger log = LoggerFactory.getLogger(InboxHelper.class);

    protected static String getIcon(String repository, String path) {
        if (ItemType.DELETED_NODE_MIXIN.equals(path)) {
            return ".resources/icons/16/document_deleted.gif";
        }
        if (StringUtils.equals(repository, "website")) {
            return ".resources/icons/16/document_plain_earth.gif";
        }

        if (StringUtils.equals(repository, "dms")) {
            String type = NodeDataUtil.getString(repository, path + "/type");
            if("folder".equals(type)){
                return ".resources/icons/16/folder.gif";
            }
            else if(StringUtils.isNotEmpty(type)){
                return StringUtils.removeStart(MIMEMapping.getMIMETypeIcon(type), "/");
            }
        }

        return ".resources/icons/16/mail.gif";
    }

    public static String getShowJSFunction(String repository, String path) {
        return "mgnl.workflow.Inbox.showFunctions." + repository;
    }
}
