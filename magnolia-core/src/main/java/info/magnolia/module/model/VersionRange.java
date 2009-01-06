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
