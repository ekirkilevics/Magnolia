/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;

/**
 * @deprecated use {@link info.magnolia.link.RelativePathTransformer} instead
 * @author had
 * @version $Id$
 */
public class RelativePathTransformer extends AbsolutePathTransformer {
    
    protected String absolutePath;

    public RelativePathTransformer(Content page, boolean useURI2RepositoryMapping, boolean useI18N) {
        super(false, useURI2RepositoryMapping, useI18N);
        UUIDLink link = new UUIDLink();
        link.setNode(page);
        link.setRepository(ContentRepository.WEBSITE);
        link.setExtension("html");
        absolutePath = super.transform(link);
    }

    public RelativePathTransformer(String absolutePath, boolean useURI2RepositoryMapping, boolean useI18N) {
        super(false, useURI2RepositoryMapping, useI18N);
        this.absolutePath = absolutePath;
    }


    public String transform(UUIDLink uuidLink) {
        String link = super.transform(uuidLink);
        return LinkHelper.makePathRelative(absolutePath, link);
    }
}
