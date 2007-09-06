/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.Server;

/**
 * Constructs URLs by prefixing the path with Server..getDefaultBaseUrl()
 *
 * @see info.magnolia.cms.beans.config.Server
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CompleteUrlPathTransformer extends AbsolutePathTransformer {

    public CompleteUrlPathTransformer(boolean useURI2RepositoryMapping, boolean useI18N) {
        super(true, useURI2RepositoryMapping, useI18N);
    }

    protected String prefixLink(String linkStr) {
        return Server.getDefaultBaseUrl() + linkStr;
    }
}
