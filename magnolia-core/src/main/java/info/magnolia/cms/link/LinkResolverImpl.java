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

import info.magnolia.link.AbsolutePathTransformer;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.link.LinkUtil;
import info.magnolia.link.LinkException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 * @deprecated since 4.0 use {@link LinkUtil} instead
 */
public class LinkResolverImpl implements LinkResolver {

    private static Logger log = LoggerFactory.getLogger(LinkResolverImpl.class);

    private boolean makeBrowserLinksRelative = false;

    private boolean addContextPathToBrowserLinks = false;
    
    public String parseLinks(String str) {
        return LinkUtil.convertAbsoluteLinksToUUIDs(str);
    }

    public String convertToEditorLinks(String str) {
        try {
            return LinkUtil.convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getEditorLink());
        } catch (LinkException e) {
            return null;
        }
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
        try {
            return LinkUtil.convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getAbsolute(addContextPath));
        } catch (LinkException e) {
            return null;
        }
    }

    public String convertToRelativeLinks(String str, String currentPath) {
        try {
            return LinkUtil.convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getRelative(currentPath));
        } catch (LinkException e) {
            return null;
        }
    }

    public String convertToExternalLinks(String str) {
        try {
            return LinkUtil.convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getCompleteUrl());
        } catch (LinkException e) {
            return null;
        }
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
