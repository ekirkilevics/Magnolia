/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.link;

import info.magnolia.cms.core.Content;
import info.magnolia.objectfactory.Components;

/**
 * Single point of access for all Link Transformers.
 * @author had
 *
 */
public class LinkTransformerManager {

    private boolean makeBrowserLinksRelative = false;
    private boolean addContextPathToBrowserLinks = false;

    public boolean isAddContextPathToBrowserLinks() {
        return this.addContextPathToBrowserLinks;
    }

    public void setAddContextPathToBrowserLinks(boolean addContextPathToBrowserLinks) {
        this.addContextPathToBrowserLinks = addContextPathToBrowserLinks;
    }

    public boolean isMakeBrowserLinksRelative() {
        return this.makeBrowserLinksRelative;
    }

    public void setMakeBrowserLinksRelative(boolean makeBrowserLinksRelative) {
        this.makeBrowserLinksRelative = makeBrowserLinksRelative;
    }

    /**
     * Gets the current singleton instance.
     */
    public static LinkTransformerManager getInstance() {
        return Components.getSingleton(LinkTransformerManager.class);
    }

    /**
     * Creates instance of absolute link transformer that will prepend the context path, will use URI2Repository mapping while constructing links and will localize the link if localization is set up.
     */
    public AbsolutePathTransformer getAbsolute() {
        return getAbsolute(true);
    }

    /**
     * Creates instance of absolute link transformer that will optionally prepend the context path, but will always use URI2Repository mapping while constructing links and will localize the link if localization is set up.
     */
    public AbsolutePathTransformer getAbsolute(boolean addContextPath) {
        return new AbsolutePathTransformer(addContextPath, true, true);
    }

    /**
     * Creates instance of Relative link transformer that will translate path to the provided Link relative to the content provided here. During the translation all valid URI2repository mappings and i18n will be applied.
     */
    public RelativePathTransformer getRelative(Content page) {
        return new RelativePathTransformer(page, true, true);
    }

    /**
     * Creates instance of Relative link transformer that will translate path to the provided Link relative to path provided here. During the translation all valid URI2repository mappings and i18n will be applied.
     */
    public RelativePathTransformer getRelative(String absolutePath) {
        return new RelativePathTransformer(absolutePath, true, true);
    }

    /**
     * Creates instance of Complete URL link transformer that will create fully qualified and localized link to content denoted by Link provided to its transform method.
     */
    public CompleteUrlPathTransformer getCompleteUrl() {
        return new CompleteUrlPathTransformer(true, true);
    }

    /**
     * @see EditorLinkTransformer
     */
    public EditorLinkTransformer getEditorLink() {
        return new EditorLinkTransformer();
    }

    /**
     * Creates instance of link transformer that will transform any provided links to either absolute or relative path based on the current server configuration.
     * @param currentPath Path to make links relative to, if relative path translation is configured on the server.
     * @return
     */
    public LinkTransformer getBrowserLink(String currentPath) {
        if (isMakeBrowserLinksRelative() ) {
            return getRelative(currentPath);
        } else {
            return getAbsolute(addContextPathToBrowserLinks);
        }
    }

    public LinkTransformer chooseLinkTransformerFor(Content content) {
        return getAbsolute();
    }
}
