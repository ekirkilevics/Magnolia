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

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class LinkResolverImpl implements LinkResolver {

    private static Logger log = LoggerFactory.getLogger(LinkResolverImpl.class);

    private boolean makeBrowserLinksRelative = false;

    private boolean addContextPathToBrowserLinks = false;

    public String parseLinks(String str) {
        // get all link tags
        Matcher matcher = LinkHelper.LINK_OR_IMAGE_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            final String href = matcher.group(4);
            if (!LinkHelper.isExternalLinkOrAnchor(href)) {
                UUIDLink link = new UUIDLink();
                try {
                    link.parseLink(href);
                    matcher.appendReplacement(res, "$1" + StringUtils.replace(link.toPattern(), "$", "\\$") + "$5");
                }
                catch (UUIDLinkException e) {
                    // this is expected if the link is an ablsolute path to something else
                    // than content stored in the repository
                    matcher.appendReplacement(res, "$0");
                    log.debug("can't parse link", e);
                }
            }
            else{
                matcher.appendReplacement(res, "$0");
            }
        }
        matcher.appendTail(res);
        return res.toString();
    }

    public String convertToEditorLinks(String str) {
        return LinkHelper.convertUsingLinkTransformer(str, new EditorLinkTransformer());
    }

    public String convertToBrowserLinks(String str, String currentPath) {
        if(isMakeBrowserLinksRelative()){
            return convertToRelativeLinks(str, currentPath);
        }
        else{
            return convertToAbsoluteLinks(str, isAddContextPathToBrowserLinks());
        }
    }

    public String convertToAbsoluteLinks(String str, boolean addContextPath) {
        return LinkHelper.convertUsingLinkTransformer(str, new AbsolutePathTransformer(addContextPath, true, true));
    }

    public String convertToRelativeLinks(String str, String currentPath) {
        return LinkHelper.convertUsingLinkTransformer(str, new RelativePathTransformer(currentPath, true, true));
    }

    public String convertToExternalLinks(String str) {
        return LinkHelper.convertUsingLinkTransformer(str, new CompleteUrlPathTransformer(true, true));
    }

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

}
