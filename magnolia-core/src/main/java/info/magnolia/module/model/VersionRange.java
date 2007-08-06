/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.model;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class VersionRange {
    private static final char separator = '/';

    private final Version from;
    private final Version to;

    public VersionRange(String rangeDef) {
        final int sepIdx = rangeDef.indexOf(separator);
        if (sepIdx < 0) {
            this.from = newVersion(rangeDef, Version.UNDEFINED_FROM);
            this.to = newVersion(rangeDef, Version.UNDEFINED_TO);
        } else {
            this.from = newVersion(rangeDef.substring(0, sepIdx), Version.UNDEFINED_FROM);
            this.to = newVersion(rangeDef.substring(sepIdx + 1), Version.UNDEFINED_TO);
        }
        validate();
    }

    public VersionRange(Version from, Version to) {
        this.from = from;
        this.to = to;
        validate();
    }

    private Version newVersion(String rangeDef, Version ifUndefined) {
        if ("*".equals(rangeDef.trim())) {
            return ifUndefined;
        }
        return Version.parseVersion(rangeDef);
    }

    private void validate() {
        if (from.isStrictlyAfter(to)) {
            throw new IllegalArgumentException("Invalid range: " + from + "/" + to);
        }
    }

    public Version getFrom() {
        return from;
    }

    public Version getTo() {
        return to;
    }

    public boolean contains(Version other) {
        return other.isEquivalent(from) || (other.isStrictlyAfter(from) && other.isBeforeOrEquivalent(to));
    }

    public String toString() {
        return from + "/" + to;
    }
}
