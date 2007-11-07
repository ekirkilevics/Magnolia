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

import info.magnolia.cms.util.FactoryUtil;

/**
 * Use to transform links
 * <ul>
 * <li> uuid pattern --> absolute links
 * <li> uuid pattern --> relative links
 * <li> internal links --> uuid pattern
 * </ul>
 * <p>
 * The internal links are for example used in the fck editor.<br/>
 * website:/home.html
 * </p>
 * <p>
 * The uuid pattern stores all the needed information like repository, uuid, path, filename (for binaries), ...
 * </p>
 * <p>
 * The absolute links are the links used for the request including all tranformations like adding context, i18n content support, repository to uri mapping, ...
 * </p>
 *
 * @author philipp
 * @version $Id$
 *
 */
public interface LinkResolver {

    public static class Factory{
        public static LinkResolver getInstance(){
            return (LinkResolver) FactoryUtil.getSingleton(LinkResolver.class);
        }
    }

    public String convertToRelativeLinks(String str, String currentPath);

    public String convertToEditorLinks(String str);

    /**
     * This method is used to create the public links. So it is used by the cms:out tag for example.
     */
    public String convertToBrowserLinks(String str, String currentPath);

    public String convertToAbsoluteLinks(String str, boolean addContextPath);

    /**
     * Used to render links usable from external (like sending an email, ...)
     */
    public String convertToExternalLinks(String str);

    /**
     * Parses the internal links (used mainly by the fckeditor) and creates the uuid pattern instead.
     */
    public String parseLinks(String str);

}
