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

import info.magnolia.cms.util.LinkUtil;

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
        return !LinkUtil.EXTERNAL_LINK_PATTERN.matcher(href).matches() && !href.startsWith("/") && !href.startsWith("#");
    }
}
