/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.link.LinkResolver;
import info.magnolia.cms.link.PathToLinkTransformer;

import org.apache.commons.lang.StringUtils;

/**
 * Util to store links in a format so that one can make relative pathes on the public site. Later we will store the
 * UUID, but in the current version the UUID is changeing during activation!
 * <p>
 * It stores the links in the following format: ${link:{uuid:{},path:{}}}. We store allready the UUID.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public final class LinkUtil {

    public static final String DEFAULT_EXTENSION = "html";

    public static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    /**
     * Util has no public constructor
     */
    private LinkUtil() {
    }

    /**
     * Determines if the given link is internal and relative.
     * @deprecated Use {@link LinkHelper#isInternalRelativeLink(String)} instead
     */
    public static boolean isInternalRelativeLink(String href) {
        return LinkHelper.isInternalRelativeLink(href);
    }

    /**
     * Transforms all the links to the magnolia format. Used during storing.
     * @param str html
     * @return html with changed hrefs
     */
    public static String convertAbsoluteLinksToUUIDs(String str) {
        return getLinkResolver().parseLinks(str);
    }

    /**
     * Maps a path to a repository. The URI2RepositoryManager is used.
     * @param path
     * @return
     */
    public static String mapPathToRepository(String path) {
        String repository = URI2RepositoryManager.getInstance().getRepository(path);
        if(StringUtils.isEmpty(repository)){
            repository = DEFAULT_REPOSITORY;
        }
        return repository;
    }

    /**
     * Convert the mangolia format to absolute (repository friendly) pathes
     * @param str html
     * @return html with absolute links
     * @deprecated
     */
    public static String convertUUIDsToAbsoluteLinks(String str) {
        return convertUUIDsToAbsoluteLinks(str, false);
    }

    public static String convertUUIDsToAbsoluteLinks(String str, boolean addContextPath) {
        return getLinkResolver().convertToAbsoluteLinks(str, addContextPath);
    }

    /**
     * @deprecated use convertUUIDsToRelativeLinks(String str, String url)
     */
    public static String convertUUIDsToRelativeLinks(String str, final Content page) {
        return convertUUIDsToRelativeLinks(str, page.getHandle());
    }

    /**
     * Transforms stored magnolia style links to relative links. This is used to display them in the browser
     * @param str html
     * @param url the links are relative to this url
     * @return html with proper links
     */
    public static String convertUUIDsToRelativeLinks(String str, String url) {
        return getLinkResolver().convertToRelativeLinks(str, url);
    }

    public static String convertUUIDsToLinks(String str, PathToLinkTransformer transformer) {
        return LinkHelper.convertUsingLinkTransformer(str, transformer);
    }

    /**
     * @deprecated use convertUUIDsToBrowserLinks(String str, String url)
     */
    public static String convertUUIDsToBrowserLinks(String str, Content content) {
        return convertUUIDsToBrowserLinks(str, content.getHandle());
    }

    public static String convertUUIDsToBrowserLinks(String str, String url) {
        return getLinkResolver().convertToBrowserLinks(str, url);
    }

    public static String convertUUIDsToEditorLinks(String str) {
        return getLinkResolver().convertToEditorLinks(str);
    }


    public static LinkResolver getLinkResolver(){
        return LinkResolver.Factory.getInstance();
    }

    /**
     * @deprecated pass the repository name
     */
    public static String makeAbsolutePathFromUUID(String uuid) {
        return LinkHelper.convertUUIDtoAbsolutePath(uuid, DEFAULT_REPOSITORY);
    }

    /**
     * Transforms a uuid to a absolute path beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links
     * @param uuid uuid
     * @return path
     * @deprecated Use {@link LinkHelper#convertUUIDtoAbsolutePath(String,String)} instead
     */
    public static String makeAbsolutePathFromUUID(String uuid, String repository) {
        return LinkHelper.convertUUIDtoAbsolutePath(uuid, repository);
    }

    /**
     * @deprecated use makeRelativePath(String absolutePath, String url)
     */
    public static String makeRelativePath(String absolutePath, Content page) {
       return  makeRelativePath(page.getHandle(), absolutePath);
    }

    /**
     * Make a absolute path relative. It adds ../ until the root is reached
     * @param absolutePath absolute path
     * @param page page to be relative to
     * @return relative path
     */
    public static String makeRelativePath(String absolutePath, String url) {
       return  LinkHelper.makePathRelative(url, absolutePath);
    }

    /**
     * @deprecated pass the repository name
     * @param path
     * @return
     */
    public static String makeUUIDFromAbsolutePath(String path) {
        return makeUUIDFromAbsolutePath(path, DEFAULT_REPOSITORY);
    }

    /**
     * Convert a path to a uuid
     * @param path path to the page
     * @return the uuid if found
     * @deprecated Use {@link LinkHelper#convertAbsolutePathToUUIDLink(String,String)} instead
     */
    public static String makeUUIDFromAbsolutePath(String path, String repository) {
        return ContentUtil.path2uuid(repository, path);
    }

    /**
     * Appends a parameter to the given url, using ?, or & if there are already
     * parameters in the given url. <strong>Warning:</strong> It does not
     * <strong>replace</strong> an existing parameter with the same name.
     */
    public static void addParameter(StringBuffer uri, String name, String value) {
        LinkHelper.addParameter(uri, name, value);
    }


}
