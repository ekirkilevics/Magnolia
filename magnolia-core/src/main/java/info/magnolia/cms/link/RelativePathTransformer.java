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
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;

public class RelativePathTransformer extends AbsolutePathTransformer{
    Content page;
    String pageLink;

    public RelativePathTransformer(Content page, boolean useURI2RepositoryMapping, boolean useI18N) {
        super(false, useURI2RepositoryMapping, useI18N);
        this.page = page;
        UUIDLink link = new UUIDLink();
        link.setNode(page);
        link.setRepository(ContentRepository.WEBSITE);
        link.setExtension("html");
        pageLink = super.transform(link);
    }

    public String transform(UUIDLink uuidLink) {
        String link = super.transform(uuidLink);
        return LinkHelper.makePathRelative(pageLink, link);
    }
}