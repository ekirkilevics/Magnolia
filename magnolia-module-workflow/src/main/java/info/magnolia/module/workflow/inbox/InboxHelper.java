/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.workflow.inbox;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.util.NodeDataUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class InboxHelper {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(InboxHelper.class);

    protected static String getIcon(String repository, String path) {
        if (StringUtils.equals(repository, "website")) {
            return ".resources/icons/16/document_plain_earth.gif";
        }

        if (StringUtils.equals(repository, "dms")) {
            String type = NodeDataUtil.getString(repository, path + "/type");
            if("folder".equals(type)){
                return ".resources/icons/16/folder.gif";
            }
            else{
                return StringUtils.removeStart(MIMEMapping.getMIMETypeIcon(type), "/");
            }
        }

        return ".resources/icons/16/mail.gif";
    }

    public static String getShowJSFunction(String repository, String path) {
        return "mgnl.workflow.Inbox.showFunctions." + repository;
    }
}
