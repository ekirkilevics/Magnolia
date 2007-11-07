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

    public String UUIDPatternsToExternalLinks(String str) {
        return LinkHelper.convertUsingLinkTransformer(str, new CompleteUrlPathTransformer(true, true));
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
