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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;


/**
 * @author philipp
 * @version $Id$
 */
public class DefaultVirtualURIMapping implements VirtualURIMapping {

    private String fromURI;

    private UrlPattern pattern;

    private String toURI;

    public MappingResult mapURI(String uri) {

        if (pattern != null && pattern.match(uri)) {
            MappingResult r = new MappingResult();
            r.setLevel(pattern.getLength());
            r.setToURI(toURI);
            return r;
        }
        return null;
    }

    public String getFromURI() {
        return this.fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;
        this.pattern = new SimpleUrlPattern(fromURI);
    }

    public String getToURI() {
        return this.toURI;
    }

    public void setToURI(String toURI) {
        this.toURI = toURI;
    }

    public String toString() {
        return fromURI + " --> " + toURI;
    }

}
