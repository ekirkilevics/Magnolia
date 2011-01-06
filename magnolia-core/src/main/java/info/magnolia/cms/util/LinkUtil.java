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
package info.magnolia.cms.util;

import info.magnolia.cms.link.LinkResolverImpl;
import info.magnolia.link.LinkTransformer;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.link.LinkException;

/**
 * Utility class to store links in a format so that one can make relative paths on the public site. Later we will store the
 * UUID, but in the current version the UUID is changing during activation!
 * <p>
 * It stores the links in the following format: ${link:{uuid:{},path:{}}}. We store already the UUID.
 * @author philipp
 * @version $Revision$ ($Author$)
 * @deprecated use {@link info.magnolia.link.LinkUtil} instead
 */
public final class LinkUtil extends info.magnolia.link.LinkUtil {
    /**
     * @deprecated use {@link info.magnolia.link.LinkUtil#makePathRelative(String, String)} instead
     */
    public static String makeRelativePath(String absolutePath, String url) {
       return  makePathRelative(url, absolutePath);
    }

    /**
     * @deprecated use {@link info.magnolia.link.LinkUtil#convertLinksFromUUIDPattern(String, LinkTransformer)} instead
     */
    public static String convertUUIDsToLinks(String str, LinkTransformer transformer) {
        try {
            return convertLinksFromUUIDPattern(str, transformer);
        } catch (LinkException e) {
            return null;
        }
    }

    /**
     * @deprecated use {@link info.magnolia.link.LinkUtil#convertToAbsoluteLinks(String, boolean)} instead
     */
    public static String convertUUIDsToAbsoluteLinks(String str, boolean addContextPath) {
        try {
            return convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getAbsolute(addContextPath));
        } catch (LinkException e) {
            return null;
        }
    }

    /**
     * @deprecated use {@link info.magnolia.link.LinkUtil#convertToRelativeLinks(String, String)} instead
     */
    public static String convertUUIDsToRelativeLinks(String str, String url) {
        try {
            return convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getRelative(url));
        } catch (LinkException e) {
            return null;
        }
    }

    /**
     * @deprecated use {@link info.magnolia.link.LinkUtil#convertLinksFromUUIDPattern(String, LinkTransformer)} instead using BrowserLinkTransformer
     */
    public static String convertUUIDsToBrowserLinks(String str, String url) {
        try {
            return convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getBrowserLink(url));
        } catch (LinkException e) {
            return null;
        }
    }

    /**
     * @deprecated use {@link info.magnolia.link.LinkUtil#convertToEditorLinks(String, String)} instead
     */
    public static String convertUUIDsToEditorLinks(String str) {
        return new LinkResolverImpl().convertToEditorLinks(str);
    }

}
