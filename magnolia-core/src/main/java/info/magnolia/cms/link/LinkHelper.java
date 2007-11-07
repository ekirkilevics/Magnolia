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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class LinkHelper {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LinkHelper.class);

    public static String makePathRelative(String from, String to){
        String fromPath = StringUtils.substringBeforeLast(from, "/");
        String toPath = StringUtils.substringBeforeLast(to, "/");

        String[] fromDirectories = StringUtils.split(fromPath, "/");
        String[] toDirectories = StringUtils.split(toPath, "/");

        int pos=0;
        while(pos < fromDirectories.length && pos < toDirectories.length && fromDirectories[pos].equals(toDirectories[pos])){
            pos++;
        }

        String rel = "";
        for(int i=pos; i < fromDirectories.length; i++ ){
            rel += "../";
        }

        for(int i=pos; i < toDirectories.length; i++ ){
            rel = rel + toDirectories[i] + "/";
        }

        rel += StringUtils.substringAfterLast(to, "/");

        return rel;
    }

    /**
     * Determines if the given link is internal and relative.
     */
    public static boolean isInternalRelativeLink(String href) {
        // TODO : this could definitely be improved
        return !isExternalLinkOrAnchor(href) && !href.startsWith("/");
    }

    public static boolean isExternalLinkOrAnchor(String href) {
       return LinkHelper.EXTERNAL_LINK_PATTERN.matcher(href).matches() || href.startsWith("#");
    }

    /**
     * Pattern that matches external and mailto: links.
     */
    public static final Pattern EXTERNAL_LINK_PATTERN = Pattern.compile("^(\\w*://|mailto:|javascript:).*");
    /**
     * Pattern to find a link
     */
    public static final Pattern LINK_OR_IMAGE_PATTERN = Pattern.compile(
        "(<(a|img) " + // start <a or <img
        "[^>]*" +  // some attributes
        "(href|src)[ ]*=[ ]*\")" + // start href or src
        "([^\"]*)" + // the link
        "(\"" + // ending "
        "[^>]*" + // any attributes
        ">)"); // end the tag

    /**
     * Appends a parameter to the given url, using ?, or & if there are already
     * parameters in the given url. <strong>Warning:</strong> It does not
     * <strong>replace</strong> an existing parameter with the same name.
     */
    public static void addParameter(StringBuffer uri, String name, String value) {
        if (uri.indexOf("?") < 0) {
            uri.append('?');
        } else {
            uri.append('&');
        }
        uri.append(name).append('=');
        try {
            uri.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("It seems your system does not support UTF-8 !?", e);
        }
    }

    /**
     * Transforms a uuid to a absolute path beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links
     * @param uuid uuid
     * @return path
     */
    public static String convertUUIDtoAbsolutePath(String uuid, String repository) {
        UUIDLink link = new UUIDLink();
        link.setRepository(repository);
        link.setUUID(uuid);
        return link.getHandle();
    }

    public static UUIDLink convertAbsolutePathToUUIDLink(String path) throws UUIDLinkException {
        return new UUIDLink().parseLink(path);
    }
    
    /**
     * Convert a path to a uuid
     * @param path path to the page
     * @return the uuid if found
     * @throws UUIDLinkException 
     */
    public static String convertAbsolutePathToUUID(String path) throws UUIDLinkException {
        return convertAbsolutePathToUUIDLink(path).getUUID();
    }

    public static String convertUsingLinkTransformer(String str, PathToLinkTransformer transformer) {
        Matcher matcher = UUIDLink.UUID_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            UUIDLink link = new UUIDLink().initByUUIDPatternMatcher(matcher);
            matcher.appendReplacement(res, transformer.transform(link));
        }
        matcher.appendTail(res);
        return res.toString();
    }

}
